package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

public record StorageResponse(
    long id,
    long userId,
    String name,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) {}
