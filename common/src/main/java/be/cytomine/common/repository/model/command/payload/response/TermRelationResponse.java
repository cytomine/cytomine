package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

public record TermRelationResponse(long id, long term1Id, long term2Id, long ontologyId, long relationId,
                                   LocalDateTime updated, Optional<LocalDateTime> deleted, LocalDateTime created,
                                   String name) implements ApplyCommandResponse {
}
