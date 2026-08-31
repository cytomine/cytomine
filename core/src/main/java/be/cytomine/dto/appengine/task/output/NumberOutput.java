package be.cytomine.dto.appengine.task.output;

import java.util.UUID;

public record NumberOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    Double value
) implements TaskRunOutput {}
