package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record OntologyCommandPayload(long id, String name, LocalDateTime created, LocalDateTime updated,
                                     Optional<LocalDateTime> deleted) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
