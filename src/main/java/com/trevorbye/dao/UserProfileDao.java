package com.trevorbye.dao;

import com.trevorbye.model.UserProfileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileDao extends CrudRepository<UserProfileEntity, Long> {

    UserProfileEntity findByUsername(String username);

    UserProfileEntity findByEmail(String email);

    @SuppressWarnings("unchecked")
    UserProfileEntity save(UserProfileEntity entity);

    List<UserProfileEntity> findAll();
}
