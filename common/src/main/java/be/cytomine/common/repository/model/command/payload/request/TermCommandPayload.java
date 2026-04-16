package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

public record TermCommandPayload(Optional<Long> parent, long id, String name, String color, LocalDateTime created,
                                 LocalDateTime updated, Optional<LocalDateTime> deleted, String comment,
                                 long ontology) {
}
