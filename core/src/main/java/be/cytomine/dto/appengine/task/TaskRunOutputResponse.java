package be.cytomine.dto.appengine.task;

import be.cytomine.dto.appengine.task.type.TaskParameterType;

public record TaskRunOutputResponse(
    String id,
    String name,
    String displayName,
    String description,
    boolean optional,
    TaskParameterType type,
    String derivedFrom
) {}
