package com.trevorbye.service;

import com.trevorbye.model.FavoriteTrackerEntity;

public interface FavoriteTrackerService {

    FavoriteTrackerEntity persistCombination(FavoriteTrackerEntity entity);

    FavoriteTrackerEntity findCombination(long postId, String username);
}
