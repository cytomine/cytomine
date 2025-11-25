package be.cytomine.dto.appengine.task.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeometryType(String id) implements TaskParameterType {}
