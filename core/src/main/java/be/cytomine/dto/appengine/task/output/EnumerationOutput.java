package be.cytomine.dto.appengine.task.output;

import java.util.UUID;

public record EnumerationOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    String value
) implements TaskRunOutput {}
