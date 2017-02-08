package com.trevorbye.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "favorite_tracker_entity")
public class FavoriteTrackerEntity {

    @Id
    private long postId;

    private String username;

    public FavoriteTrackerEntity() {
    }

    public FavoriteTrackerEntity(long postId, String username) {
        this.postId = postId;
        this.username = username;
    }

    public long getPostId() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "FavoriteTrackerEntity{" +
                "postId=" + postId +
                ", username='" + username + '\'' +
                '}';
    }
}
