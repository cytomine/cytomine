package be.cytomine.dto.project;

import java.util.List;

public record ProjectFromSearchRequest(
    String name,
    String ontologyMode,
    Long ontologyId,
    String query,
    List<String> filters
) {
}