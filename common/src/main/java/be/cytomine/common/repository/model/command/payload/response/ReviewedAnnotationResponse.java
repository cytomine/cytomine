package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record ReviewedAnnotationResponse(long id, long userId, long reviewUserId,
                                         long imageId, long sliceId, long projectId,
                                         long parentIdent, String parentClassName, int status,
                                         String wktLocation, double geometryCompression,
                                         List<Long> termIds,
                                         LocalDateTime created, LocalDateTime updated,
                                         Optional<LocalDateTime> deleted) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.REVIEWED_ANNOTATION;
    }
}
