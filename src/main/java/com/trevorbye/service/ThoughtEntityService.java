package com.trevorbye.service;

import com.trevorbye.model.ThoughtEntity;

import java.util.List;

public interface ThoughtEntityService {
    ThoughtEntity save(ThoughtEntity thoughtEntity);
    ThoughtEntity findPostById(Long postId);
    List<ThoughtEntity> findByUser(String username);
    List<ThoughtEntity> getDescendingThoughtArray();
    Long deleteEntity(Long postId);
}
