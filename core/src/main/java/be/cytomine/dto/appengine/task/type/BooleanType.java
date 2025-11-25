package be.cytomine.dto.appengine.task.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BooleanType(String id) implements TaskParameterType {}
