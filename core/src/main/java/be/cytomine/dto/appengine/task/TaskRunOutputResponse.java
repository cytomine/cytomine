package be.cytomine.dto.appengine.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import be.cytomine.dto.appengine.task.type.TaskParameterType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskRunOutputResponse(
    String id,
    String name,
    String displayName,
    String description,
    boolean optional,
    TaskParameterType type,
    String derivedFrom
) {}
