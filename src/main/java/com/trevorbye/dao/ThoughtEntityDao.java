package com.trevorbye.dao;

import com.trevorbye.model.ThoughtEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ThoughtEntityDao extends CrudRepository<ThoughtEntity, Long> {

    @SuppressWarnings("unchecked")
    ThoughtEntity save(ThoughtEntity entity);

    ThoughtEntity findOne(Long postId);

    List<ThoughtEntity> findAllByUsername(String username);

    List<ThoughtEntity> findFirst5ByOrderByPostDateDesc();

    @Transactional
    Long deleteByPostId(Long postId);

    List<ThoughtEntity> findByUsernameOrderByFavoriteCountDesc(String username);

}
