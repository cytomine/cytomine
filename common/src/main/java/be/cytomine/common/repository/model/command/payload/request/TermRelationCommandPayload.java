package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record TermRelationCommandPayload(long id, long term1Id, long term2Id, long ontologyId, long relationId,
                                         LocalDateTime updated, Optional<LocalDateTime> deleted, LocalDateTime created,
                                         String name) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return ontologyId;
    }
}
