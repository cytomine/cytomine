package org.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.common.repository.model.HasAclId;
import org.cytomine.common.repository.model.HasLongId;

public record RoleCommandPayload(
    long id,
    String authority,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
