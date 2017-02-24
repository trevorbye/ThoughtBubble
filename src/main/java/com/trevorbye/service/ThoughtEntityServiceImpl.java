package com.trevorbye.service;

import com.trevorbye.dao.ThoughtEntityDao;
import com.trevorbye.model.ThoughtEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ThoughtEntityServiceImpl implements ThoughtEntityService {

    @Autowired
    private ThoughtEntityDao entityDao;

    @Override
    public ThoughtEntity save(ThoughtEntity thoughtEntity) {
        return entityDao.save(thoughtEntity);
    }

    @Override
    public ThoughtEntity findPostById(Long postId) {
        return entityDao.findOne(postId);
    }

    @Override
    public List<ThoughtEntity> findByUser(String username) {
        return entityDao.findAllByUsername(username);
    }

    @Override
    public List<ThoughtEntity> getDescendingThoughtArray() {
        return entityDao.findFirst5ByOrderByPostDateDesc();
    }

    @Override
    public Long deleteEntity(Long postId) {
        return entityDao.deleteByPostId(postId);
    }

    @Override
    public List<ThoughtEntity> findProfileThoughts(String username) {
        return entityDao.findFirst5ByUsernameOrderByFavoriteCountDesc(username);
    }


}
