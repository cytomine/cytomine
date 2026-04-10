package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

public record RelationResponse(String name, long id, LocalDateTime created,
                               LocalDateTime updated, Optional<LocalDateTime> deleted) {
}
