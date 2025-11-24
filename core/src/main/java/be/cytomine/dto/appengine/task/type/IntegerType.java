package be.cytomine.dto.appengine.task.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntegerType(String id,
    Integer gt,
    Integer geq,
    Integer lt,
    Integer leq
) implements TaskParameterType {}
