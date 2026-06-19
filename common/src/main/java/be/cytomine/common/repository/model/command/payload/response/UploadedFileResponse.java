package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.command.DataType;

public record UploadedFileResponse(
    long id,
    Optional<Long> user,
    Optional<Long> parent,
    Optional<Long> storage,
    String filename,
    String originalFilename,
    String ext,
    String contentType,
    Long size,
    String path,
    int status,
    Set<Long> projects,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted,
    Optional<String> thumbnailUrl
) implements ApplyCommandResponse {

    public UploadedFileResponse {
        if (deleted == null) {
            deleted = Optional.empty();
        }
        if (thumbnailUrl == null) {
            thumbnailUrl = Optional.empty();
        }
    }

    public static UploadedFileResponse withThumbnailUrl(UploadedFileResponse r, Optional<String> thumbnailUrl) {
        return new UploadedFileResponse(
            r.id(),
            r.user(),
            r.parent(),
            r.storage(),
            r.filename(),
            r.originalFilename(),
            r.ext(),
            r.contentType(),
            r.size(),
            r.path(),
            r.status(),
            r.projects(),
            r.created(),
            r.updated(),
            r.deleted(),
            thumbnailUrl
        );
    }

    @Override
    public DataType getDataType() {
        return DataType.UPLOADED_FILE;
    }
}
