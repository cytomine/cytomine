package be.cytomine.appengine.dto.inputs.task;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TaskInput(
    String id,
    @JsonProperty(value = "default") String defaultValue,
    String name,
    @JsonProperty(value = "display_name") String displayName,
    String description,
    boolean optional,
    TaskParameterType type
) {}
