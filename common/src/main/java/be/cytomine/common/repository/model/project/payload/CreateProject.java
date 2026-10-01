package be.cytomine.common.repository.model.project.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateProject(
    @NotEmpty String name,
    Long ontology,
    boolean blindMode,
    boolean areImagesDownloadable,
    boolean isClosed,
    boolean isRestricted,
    boolean isReadOnly,
    boolean hideUsersLayers,
    boolean hideAdminsLayers
) {}
