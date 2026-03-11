package be.cytomine.appengine.dto.inputs.task;

public record TaskOutput(
    String id,
    String name,
    String displayName,
    String description,
    boolean optional,
    TaskParameterType type,
    String derivedFrom
) {}
