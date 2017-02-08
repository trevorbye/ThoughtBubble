package com.trevorbye.POJO;

import com.trevorbye.model.UserProfileEntity;

/**
 * Created by trevorBye on 1/30/17.
 */
public class UserProfileHalWrapper extends HALResource {
    private UserProfileEntity userProfileEntity;

    public UserProfileHalWrapper() {
    }

    public UserProfileHalWrapper(UserProfileEntity userProfileEntity) {
        this.userProfileEntity = userProfileEntity;
    }

    public UserProfileEntity getUserProfileEntity() {
        return userProfileEntity;
    }

    @Override
    public String toString() {
        return "UserProfileHalWrapper{" +
                "userProfileEntity=" + userProfileEntity +
                '}';
    }
}
