package be.cytomine.common.repository.model.command.payload.response;

import java.time.Instant;
import java.util.Optional;

public record RelationResponse(
    String name,
    long id,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted
) {}
