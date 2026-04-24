package be.cytomine.appengine.dto.inputs.task;

import java.util.Set;
import java.util.UUID;

public record TaskDescription(
    UUID id,
    String name,
    String namespace,
    String version,
    String description,
    Set<TaskAuthor> authors
) {
    public TaskDescription {
        description = description == null ? "" : description;
    }
}
