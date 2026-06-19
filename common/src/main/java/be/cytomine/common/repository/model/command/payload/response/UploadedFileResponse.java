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
    }

    @Override
    public DataType getDataType() {
        return DataType.UPLOADED_FILE;
    }
}
