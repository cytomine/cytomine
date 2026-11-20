package org.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.common.repository.model.HasAclId;
import org.cytomine.common.repository.model.HasLongId;

public record ProjectCommandPayload(
    long id,
    String name,
    Long ontologyId,
    boolean blindMode,
    boolean areImagesDownloadable,
    boolean isClosed,
    String mode,
    boolean hideUsersLayers,
    boolean hideAdminsLayers,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return id;
    }
}
