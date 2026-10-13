package be.cytomine.common.repository.model.project.payload;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateProject(
    Optional<String> name,
    Optional<Long> ontology,
    Optional<Boolean> blindMode,
    Optional<Boolean> areImagesDownloadable,
    Optional<Boolean> isClosed,
    Optional<Boolean> isRestricted,
    Optional<Boolean> isReadOnly,
    Optional<Boolean> hideUsersLayers,
    Optional<Boolean> hideAdminsLayers
) {}
