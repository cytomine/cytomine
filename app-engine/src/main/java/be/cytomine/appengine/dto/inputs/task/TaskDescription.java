package be.cytomine.appengine.dto.inputs.task;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record TaskDescription(
    UUID id,
    String name,
    String namespace,
    String version,
    Optional<String> description,
    Set<TaskAuthor> authors
) {}
