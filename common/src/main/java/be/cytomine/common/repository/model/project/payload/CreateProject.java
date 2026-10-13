package be.cytomine.common.repository.model.project.payload;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateProject(
    @NotEmpty String name,
    Optional<Long> ontology,
    boolean blindMode,
    boolean areImagesDownloadable,
    boolean isClosed,
    boolean isRestricted,
    boolean isReadOnly,
    boolean hideUsersLayers,
    boolean hideAdminsLayers
) {}
