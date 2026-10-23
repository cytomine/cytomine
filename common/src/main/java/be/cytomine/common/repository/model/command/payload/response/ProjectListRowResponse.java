package be.cytomine.common.repository.model.command.payload.response;

import java.time.LocalDateTime;
import java.util.Optional;

public record ProjectListRowResponse(
    long id,
    String name,
    Optional<Long> ontology,
    Optional<String> ontologyName,
    boolean blindMode,
    boolean areImagesDownloadable,
    long numberOfSlides,
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
    long membersCount,
    ProjectUserRoles currentUserRoles,
    Optional<Long> lastActivity
) {}
