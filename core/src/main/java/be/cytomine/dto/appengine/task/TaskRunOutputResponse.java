package be.cytomine.dto.appengine.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import be.cytomine.dto.appengine.task.type.TaskParameterType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskRunOutputResponse(
    String id,
    String name,
    @JsonProperty(value = "display_name") String displayName,
    String description,
    boolean optional,
    TaskParameterType type,
    @JsonProperty(value = "derived_from") String derivedFrom
) {}
