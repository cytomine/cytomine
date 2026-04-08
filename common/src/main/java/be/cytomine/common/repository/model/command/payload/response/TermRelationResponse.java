package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record TermRelationResponse(long id, long term1Id, long term2Id, long ontologyId, long relationId,
                                   LocalDateTime updated, Optional<LocalDateTime> deleted, LocalDateTime created,
                                   String name) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.TERM_RELATION;
    }
}
