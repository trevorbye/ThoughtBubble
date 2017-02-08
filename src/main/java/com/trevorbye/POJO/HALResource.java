package com.trevorbye.POJO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.HashMap;
import java.util.Map;

public abstract class HALResource extends ResourceSupport {

    private final Map<String, ResourceSupport> embedded = new HashMap<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("_embedded")
    public Map<String, ResourceSupport> getEmbedded() {
        return embedded;
    }

    public void embedResource(String relationship, ResourceSupport resource) {
        embedded.put(relationship,resource);
    }
}
