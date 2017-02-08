package com.trevorbye.POJO;


import com.trevorbye.model.ThoughtEntity;

import java.util.List;

//This class allows a List of ThoughtEntity objects to be returned as a HAL formatted JSON with HATEOAS support.
public class ThoughtEntityListWrapper extends HALResource {
    private List<ThoughtEntity> entityList;

    public ThoughtEntityListWrapper(List<ThoughtEntity> entityList) {
        this.entityList = entityList;
    }

    public List<ThoughtEntity> getEntityList() {
        return entityList;
    }

    @Override
    public String toString() {
        return "ThoughtEntityListWrapper{" +
                "entityList=" + entityList +
                '}';
    }
}
