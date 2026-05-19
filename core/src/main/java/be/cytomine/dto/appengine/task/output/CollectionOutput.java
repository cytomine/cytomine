package be.cytomine.dto.appengine.task.output;

import java.util.List;
import java.util.UUID;

public record CollectionOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    List<IndexedTaskRunOutput> value,
    String subType
) implements TaskRunOutput {
    public record IndexedTaskRunOutput(Object value, int index) {}
}
