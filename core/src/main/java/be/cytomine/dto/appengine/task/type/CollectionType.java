package be.cytomine.dto.appengine.task.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CollectionType(String id, int minSize, int maxSize, TaskParameterType subType) implements TaskParameterType {}
