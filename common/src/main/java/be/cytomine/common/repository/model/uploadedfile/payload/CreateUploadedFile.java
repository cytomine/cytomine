package be.cytomine.common.repository.model.uploadedfile.payload;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

public record CreateUploadedFile(
    long user,
    long storage,
    Optional<Long> parent,
    @NotEmpty String filename,
    @NotEmpty String originalFilename,
    String ext,
    String contentType,
    Long size,
    int status,
    Optional<Set<Long>> projects
) {}
