package be.cytomine.dto.appengine.task.output;

import java.util.UUID;

public record FileOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    byte[] value
) implements TaskRunOutput {}
