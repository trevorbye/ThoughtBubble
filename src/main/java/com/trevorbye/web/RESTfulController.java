package com.trevorbye.web;

import com.trevorbye.POJO.ErrorJsonResponse;
import com.trevorbye.POJO.PrincipalHalWrapper;
import com.trevorbye.POJO.ThoughtEntityListWrapper;
import com.trevorbye.POJO.UserProfileHalWrapper;
import com.trevorbye.model.FavoriteTrackerEntity;
import com.trevorbye.model.ThoughtEntity;
import com.trevorbye.model.UserProfileEntity;
import com.trevorbye.service.FavoriteTrackerService;
import com.trevorbye.service.ThoughtEntityService;
import com.trevorbye.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class RESTfulController {

    @Autowired
    private ThoughtEntityService thoughtEntityService;

    @Autowired
    private FavoriteTrackerService favoriteTrackerService;

    @Autowired
    private UserProfileService userProfileService;

    @RequestMapping(value = "/user", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<?> user(Principal principal) {
        Link selfRel = linkTo(methodOn(RESTfulController.class).user(principal)).withSelfRel();

        if (principal == null) {
            ErrorJsonResponse response = new ErrorJsonResponse("Not authenticated.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        PrincipalHalWrapper principalWrapper = new PrincipalHalWrapper(principal);
        principalWrapper.add(selfRel);

        return new ResponseEntity<>(principalWrapper, HttpStatus.OK);
    }

    @RequestMapping(value = "/register-user", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserProfileEntity profileEntity, HttpServletRequest request) throws ServletException {
        Link selfRel = linkTo(methodOn(RESTfulController.class).registerUser(profileEntity, request)).withSelfRel();

        UserProfileEntity existingEmail = userProfileService.findByEmail(profileEntity.getEmail());
        if (existingEmail != null) {
            ErrorJsonResponse response = new ErrorJsonResponse("Account already exists for this email.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        UserProfileEntity existingUser = userProfileService.findByUsername(profileEntity.getUsername());
        if (existingUser != null) {
            ErrorJsonResponse response = new ErrorJsonResponse("Username is taken.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        //save new profile if all conditions are met
        profileEntity.setEnabled(true);
        UserProfileEntity newUser = userProfileService.save(profileEntity);
        //auto login
        request.login(newUser.getUsername(), newUser.getPassword());
        //wrap user object in HAL formatted wrapper
        UserProfileHalWrapper userProfileHalWrapper = new UserProfileHalWrapper(profileEntity);
        userProfileHalWrapper.add(selfRel);

        return new ResponseEntity<>(userProfileHalWrapper, HttpStatus.OK);
    }

    @RequestMapping(value = "/getPostsByUser", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<?> postsByUser(@RequestParam(value = "username") String username) {

        Link selfRel = linkTo(methodOn(RESTfulController.class).postsByUser(username)).withSelfRel();
        List<ThoughtEntity> entityList = thoughtEntityService.findByUser(username);

        if (entityList.isEmpty()) {
            ErrorJsonResponse response = new ErrorJsonResponse("No posts found for username.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }

        //add HATEOAS HAL formatting
        ThoughtEntityListWrapper entityListWrapper = new ThoughtEntityListWrapper(entityList);
        entityListWrapper.add(selfRel);

        return new ResponseEntity<>(entityListWrapper, HttpStatus.OK);
    }


    @RequestMapping(value = "/persistThought", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<?> persistThought(@Valid @RequestBody ThoughtEntity thoughtEntity, Principal principal) {

        Link selfRel = linkTo(methodOn(RESTfulController.class).persistThought(thoughtEntity, principal)).withSelfRel();

        //reject if poster is not currently logged in user
        if(!thoughtEntity.getUsername().equals(principal.getName())) {
            ErrorJsonResponse response = new ErrorJsonResponse("Credentials conflict with resource.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Date now = new Date(Calendar.getInstance().getTimeInMillis());
        thoughtEntity.setPostDate(now);
        thoughtEntity.setFavoriteCount(0);
        thoughtEntityService.save(thoughtEntity);

        //HATEOAS self rel
        thoughtEntity.add(selfRel);

        //HATEOAS rel: delete
        ThoughtEntity deleteEntity = new ThoughtEntity(thoughtEntity);
        deleteEntity.removeLinks();
        deleteEntity.add(linkTo(methodOn(RESTfulController.class).deleteThought(deleteEntity.getPostId(), principal)).withSelfRel());


        //HATEOAS _embedded
        thoughtEntity.embedResource("delete", deleteEntity);


        return new ResponseEntity<>(thoughtEntity, HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteThought", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<?> deleteThought(@RequestParam(value = "postId") Long postId, Principal principal) {

        Link selfRel = linkTo(methodOn(RESTfulController.class).deleteThought(postId, principal)).withSelfRel();
        ThoughtEntity thoughtEntity = thoughtEntityService.findPostById(postId);

        //reject if deleter is not currently logged in user
        if(!thoughtEntity.getUsername().equals(principal.getName())) {
            ErrorJsonResponse response = new ErrorJsonResponse("Credentials conflict with resource.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        thoughtEntityService.deleteEntity(postId);
        //HATEOAS self rel
        thoughtEntity.add(selfRel);

        //HATEOAS rel: create
        ThoughtEntity createEntity = new ThoughtEntity(thoughtEntity);
        createEntity.setFavoriteCount(0);
        createEntity.removeLinks();
        createEntity.add(linkTo(methodOn(RESTfulController.class).persistThought(createEntity,principal)).withSelfRel());

        thoughtEntity.embedResource("create", createEntity);
        return new ResponseEntity<>(thoughtEntity, HttpStatus.OK);
    }

    @RequestMapping("/incrementFavoriteCount")
    public ResponseEntity<?> incrementFavoriteCount(@RequestParam(value = "postId") Long postId, Principal principal) {

        Link selfRel = linkTo(methodOn(RESTfulController.class).incrementFavoriteCount(postId,principal)).withSelfRel();
        String postCreatedBy = thoughtEntityService.findPostById(postId).getUsername();

        if (postCreatedBy.equals(principal.getName())) {
            ErrorJsonResponse response = new ErrorJsonResponse("User cannot favorite own thought.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        //verify that user hasn't already liked post
        FavoriteTrackerEntity combination = favoriteTrackerService.findCombination(postId,principal.getName());
        if (combination != null) {
            ErrorJsonResponse response = new ErrorJsonResponse("User already favorited this post.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //check if user deleted thought while still in web socket queue
        ThoughtEntity thoughtEntity = thoughtEntityService.findPostById(postId);
        if (thoughtEntity == null) {
            ErrorJsonResponse response = new ErrorJsonResponse("User has deleted this thought.");
            response.add(selfRel);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        thoughtEntity.setFavoriteCount(thoughtEntity.getFavoriteCount() + 1);
        thoughtEntityService.save(thoughtEntity);

        //persist favorite combination to disallow multiple likes
        FavoriteTrackerEntity toPersist = new FavoriteTrackerEntity(postId, principal.getName());
        favoriteTrackerService.persistCombination(toPersist);

        thoughtEntity.add(selfRel);

        return new ResponseEntity<>(thoughtEntity, HttpStatus.OK);
    }

    //endpoint for fetching data to display on user profile
    @RequestMapping("/getProfileData")
    public ResponseEntity<?> getProfileData(@RequestParam(value = "username") String username, Principal principal) {

        if (!username.equals(principal.getName())) {
            return new ResponseEntity<>(new ErrorJsonResponse("Credentials conflict with resource."), HttpStatus.UNAUTHORIZED);
        }

        return null;
    }

    @RequestMapping(value = "/getLatestPost", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity<?> getLatestPost() {
        List<ThoughtEntity> queryList = thoughtEntityService.getDescendingThoughtArray();
        ThoughtEntityListWrapper listWrapper = new ThoughtEntityListWrapper(queryList);

        listWrapper.add(linkTo(methodOn(RESTfulController.class).getLatestPost()).withSelfRel());

        return new ResponseEntity<>(listWrapper, HttpStatus.OK);
    }
}
