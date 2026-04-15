package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record ReviewedAnnotationCommandPayload(long id, long userId, long reviewUserId,
                                               long imageId, long sliceId, long projectId,
                                               long parentIdent, String parentClassName, int status,
                                               String wktLocation, double geometryCompression,
                                               List<Long> termIds,
                                               LocalDateTime created, LocalDateTime updated,
                                               Optional<LocalDateTime> deleted) {
}
