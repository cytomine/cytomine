package be.cytomine.common.repository.model.command.payload.request;

import java.time.Instant;
import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record OntologyCommandPayload(
    long id,
    String name,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted
) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
