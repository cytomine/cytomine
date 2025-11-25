package be.cytomine.dto.appengine.task.type;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FileType(String id, List<String> formats) implements TaskParameterType {}
