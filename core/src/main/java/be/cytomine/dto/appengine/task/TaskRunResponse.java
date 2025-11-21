package be.cytomine.dto.appengine.task;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskRunResponse(
    UUID id,
    String state,
    TaskDescription task
) {}
