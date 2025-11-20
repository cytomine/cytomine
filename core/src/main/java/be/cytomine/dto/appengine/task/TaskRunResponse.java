package be.cytomine.dto.appengine.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskRunResponse(
    String id,
    String state,
    TaskDescription task
) {}
