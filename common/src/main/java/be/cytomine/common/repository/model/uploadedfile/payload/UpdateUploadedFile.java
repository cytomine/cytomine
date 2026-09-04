package be.cytomine.common.repository.model.uploadedfile.payload;

import java.util.Optional;
import java.util.Set;

public record UpdateUploadedFile(
    Optional<String> filename,
    Optional<String> originalFilename,
    Optional<String> ext,
    Optional<String> contentType,
    Optional<Long> size,
    Optional<Integer> status,
    Optional<Set<Long>> projects
) {}
