package be.cytomine.dto.appengine.task.output;

import java.time.Instant;
import java.util.UUID;

public record DateTimeOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    Instant value
) implements TaskRunOutput {}
