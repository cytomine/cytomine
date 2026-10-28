package org.cytomine.repository.persistence;

import java.time.LocalDateTime;

public record ProjectListRow(
    long id,
    String name,
    Long ontology,
    String ontologyName,
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
    LocalDateTime updated,
    long membersCount,
    Long lastActivity
) {}
