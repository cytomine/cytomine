package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record UserAnnotationResponse(long id, long userId, long imageId, long sliceId, long projectId,
                                     String wktLocation, double geometryCompression,
                                     int countReviewedAnnotations,
                                     LocalDateTime created, LocalDateTime updated,
                                     Optional<LocalDateTime> deleted) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.USER_ANNOTATION;
    }
}
