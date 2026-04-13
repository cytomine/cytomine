package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

public record AnnotationTermCommandPayload(long id, long userAnnotationId, long termId, long userId,
                                           LocalDateTime created, LocalDateTime updated,
                                           Optional<LocalDateTime> deleted) {
}
