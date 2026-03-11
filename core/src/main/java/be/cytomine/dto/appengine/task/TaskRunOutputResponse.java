package be.cytomine.dto.appengine.task;

public record TaskRunOutputResponse(
    String id,
    String name,
    String displayName,
    String description,
    boolean optional,
    TaskParameterType type,
    String derivedFrom
) {}
