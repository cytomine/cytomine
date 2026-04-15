package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

public record UserAnnotationCommandPayload(long id, long userId, long imageId, long sliceId, long projectId,
                                           String wktLocation, double geometryCompression,
                                           LocalDateTime created, LocalDateTime updated,
                                           Optional<LocalDateTime> deleted) {
}
