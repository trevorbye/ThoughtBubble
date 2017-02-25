package com.trevorbye.service;

import com.trevorbye.dao.UserProfileDao;
import com.trevorbye.model.UserProfileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileDao userProfileDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserProfileEntity findByUsername(String username) {
        return userProfileDao.findByUsername(username);
    }

    @Override
    public UserProfileEntity findByEmail(String email) {
        return userProfileDao.findByEmail(email);
    }

    @Override
    public UserProfileEntity save(UserProfileEntity entity) {
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return userProfileDao.save(entity);
    }

    @Override
    public List<UserProfileEntity> findAll() {
        return userProfileDao.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserProfileEntity user = userProfileDao.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User does not exist.");
        }
        return user;
    }
}
