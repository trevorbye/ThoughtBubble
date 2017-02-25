package com.trevorbye.service;

import com.trevorbye.model.UserProfileEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserProfileService extends UserDetailsService {
    UserProfileEntity findByUsername(String username);
    UserProfileEntity findByEmail(String email);
    UserProfileEntity save(UserProfileEntity entity);
    List<UserProfileEntity> findAll();
}
