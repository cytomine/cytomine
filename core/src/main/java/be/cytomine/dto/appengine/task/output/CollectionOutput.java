package be.cytomine.dto.appengine.task.output;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import be.cytomine.jackson.CollectionOutputDeserializer;

@JsonDeserialize(using = CollectionOutputDeserializer.class)
public record CollectionOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    List<IndexedTaskRunOutput> value,
    String subType
) implements TaskRunOutput {
    public record IndexedTaskRunOutput(TaskRunOutput value, int index) {}
}
