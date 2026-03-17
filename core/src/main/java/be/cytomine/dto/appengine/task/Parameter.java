package be.cytomine.dto.appengine.task;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Parameter(
    UUID id,
    String parameterType,
    String name,
    String displayName,
    String description,
    boolean optional,
    Parameter derivedFrom
) {}
