package be.cytomine.dto.appengine.task.output;

import java.util.UUID;

public record ImageOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    byte[] value
) implements TaskRunOutput {}
