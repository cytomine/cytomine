package be.cytomine.dto.appengine.task.output;

import java.util.UUID;

public record IntegerOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    Integer value
) implements TaskRunOutput {}
