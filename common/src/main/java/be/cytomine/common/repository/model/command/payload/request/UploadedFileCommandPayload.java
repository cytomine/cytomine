package be.cytomine.common.repository.model.command.payload.request;

import java.time.Instant;
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
    Instant created,
    Optional<Instant> updated,
    Optional<Instant> deleted
) implements HasLongId, HasAclId {
    @Override
    public long aclId() {
        return storageId;
    }
}
