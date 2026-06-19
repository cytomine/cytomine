package be.cytomine.dto.image;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;

public record UploadedFileView(
    @JsonUnwrapped UploadedFileResponse file,
    Optional<String> thumbnailUrl
) {
    public static UploadedFileView from(UploadedFileResponse file, Optional<String> thumbnailUrl) {
        return new UploadedFileView(file, thumbnailUrl);
    }
}
