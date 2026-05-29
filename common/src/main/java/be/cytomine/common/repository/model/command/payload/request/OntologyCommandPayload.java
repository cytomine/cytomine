package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

public record OntologyCommandPayload(long id, String name, LocalDateTime created, LocalDateTime updated,
                                     Optional<LocalDateTime> deleted) {
}
