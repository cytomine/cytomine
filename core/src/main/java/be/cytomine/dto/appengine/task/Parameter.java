package be.cytomine.dto.appengine.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Parameter(
    UUID id,
    String parameterType,
    String name,
    String displayName,
    String description,
    boolean optional,
    Parameter derivedFrom
) { }
