package be.cytomine.common.repository.model.command.payload.request;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;

public record UploadedFileCommandPayload(
    long id,
    long storageId,
    Optional<Long> parent,
    String filename,
    String originalFilename,
    String ext,
    String contentType,
    Long size,
    int status,
    Set<Long> projects,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return storageId;
    }
}
