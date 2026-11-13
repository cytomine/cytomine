package org.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.common.repository.model.HasAclId;
import org.cytomine.common.repository.model.HasLongId;

public record UserRoleCommandPayload(long id,
                                     long userId,
                                     long roleId,
                                     LocalDateTime created,
                                     Optional<LocalDateTime> updated,
                                     Optional<LocalDateTime> deleted) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return userId;
    }
}
