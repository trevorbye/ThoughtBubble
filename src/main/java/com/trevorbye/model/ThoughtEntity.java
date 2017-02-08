package com.trevorbye.model;


import com.trevorbye.POJO.HALResource;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "thought_entity")
public class ThoughtEntity extends HALResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long postId;

    @NotEmpty
    private String username;

    @NotEmpty
    @Length(max = 254)
    private String body;

    //no validation; fields are modified server-side
    private long favoriteCount;
    private Date postDate;

    public ThoughtEntity(String username, String body, long favoriteCount, Date postDate) {
        this.username = username;
        this.body = body;
        this.favoriteCount = favoriteCount;
        this.postDate = postDate;
    }

    public ThoughtEntity() {
    }

    //clone constructor
    public ThoughtEntity(ThoughtEntity copyEntity) {
        this.postId = copyEntity.getPostId();
        this.username = copyEntity.getUsername();
        this.body = copyEntity.getBody();
        this.favoriteCount = copyEntity.getFavoriteCount();
        this.postDate = copyEntity.getPostDate();
    }

    //only provide setter to increment favorite count, date, and user. Body can remain immutable.
    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getPostId() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() {
        return body;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public Date getPostDate() {
        return postDate;
    }

    @Override
    public String toString() {
        return "ThoughtEntity{" +
                "id=" + postId +
                ", username='" + username + '\'' +
                ", body='" + body + '\'' +
                ", favoriteCount=" + favoriteCount +
                ", postDate=" + postDate +
                '}';
    }
}
