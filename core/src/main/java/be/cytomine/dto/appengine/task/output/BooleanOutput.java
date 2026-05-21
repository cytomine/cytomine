package be.cytomine.dto.appengine.task.output;

import java.util.UUID;

public record BooleanOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    Boolean value
) implements TaskRunOutput {}
