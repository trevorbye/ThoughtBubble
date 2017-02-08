package com.trevorbye.service;

import com.trevorbye.dao.FavoriteTrackerDao;
import com.trevorbye.model.FavoriteTrackerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteTrackerServiceImpl implements FavoriteTrackerService {

    @Autowired
    private FavoriteTrackerDao favoriteTrackerDao;

    @Override
    public FavoriteTrackerEntity persistCombination(FavoriteTrackerEntity entity) {
        return favoriteTrackerDao.save(entity);
    }

    @Override
    public FavoriteTrackerEntity findCombination(long postId, String username) {
        return favoriteTrackerDao.findByPostIdAndUsername(postId, username);
    }
}
