package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record AnnotationTermResponse(long id, long userAnnotationId, long termId, long userId,
                                     LocalDateTime created, LocalDateTime updated,
                                     Optional<LocalDateTime> deleted) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.ANNOTATION_TERM;
    }
}
