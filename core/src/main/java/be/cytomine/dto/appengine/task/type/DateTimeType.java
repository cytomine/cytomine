package be.cytomine.dto.appengine.task.type;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DateTimeType(
    String id,
    Instant before,
    Instant after
) implements TaskParameterType {}
