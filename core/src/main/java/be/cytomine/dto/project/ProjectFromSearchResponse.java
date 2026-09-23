package be.cytomine.dto.project;

public record ProjectFromSearchResponse(
    ProjectReference project,
    TaskReference task
) {
    public record ProjectReference(Long id, String name) {
    }

    public record TaskReference(Long id, int progress, Long project, Long user, boolean printInActivity) {
    }
}