package be.cytomine.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProjectFromSearchResponse(
    ProjectReference project,
    TaskReference task
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProjectReference(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TaskReference(Long id, int progress, Long project, Long user, boolean printInActivity) {
    }
}