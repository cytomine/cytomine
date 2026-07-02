package be.cytomine.common.repository.model.command.payload.request;

import java.time.Instant;
import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record TagDomainAssociationCommandPayload(
    long id,
    long tagId,
    String domainClassName,
    long domainId,
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted
) implements HasLongId, HasAclId {

    @Override
    public long aclId() {
        return tagId;
    }
}
