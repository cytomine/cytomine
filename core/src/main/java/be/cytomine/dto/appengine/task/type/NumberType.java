package be.cytomine.dto.appengine.task.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NumberType(
    String id,
    Double gt,
    Double geq,
    Double lt,
    Double leq,
    boolean infinityAllowed,
    boolean nanAllowed
) implements TaskParameterType {}
