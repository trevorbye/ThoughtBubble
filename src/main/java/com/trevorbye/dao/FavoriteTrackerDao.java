package com.trevorbye.dao;

import com.trevorbye.model.FavoriteTrackerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteTrackerDao extends CrudRepository<FavoriteTrackerEntity, Long> {

    @SuppressWarnings("unchecked")
    FavoriteTrackerEntity save(FavoriteTrackerEntity entity);

    FavoriteTrackerEntity findByPostIdAndUsername(long postId, String username);
}
