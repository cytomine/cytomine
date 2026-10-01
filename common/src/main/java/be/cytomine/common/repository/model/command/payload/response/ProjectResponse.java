package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

import be.cytomine.common.repository.model.command.DataType;

public record ProjectResponse(
    long id,
    String name,
    Optional<Long> ontology,
    Optional<String> ontologyName,
    boolean blindMode,
    boolean areImagesDownloadable,
    long numberOfImages,
    long numberOfAnnotations,
    long numberOfJobAnnotations,
    long numberOfReviewedAnnotations,
    boolean isClosed,
    boolean isReadOnly,
    boolean isRestricted,
    boolean hideUsersLayers,
    boolean hideAdminsLayers,
    LocalDateTime created,
    Optional<LocalDateTime> updated,
    Optional<LocalDateTime> deleted
) implements ApplyCommandResponse {
    @Override
    public DataType getDataType() {
        return DataType.PROJECT;
    }
}
