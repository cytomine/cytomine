package be.cytomine.common.repository.model;

import java.util.Set;

public record TermResponse(long id, String name, String color, long ontologyId, Set<TermResponse> children) {
    public TermResponse {
        if (children == null) {
            children = Set.of();
        }
    }
}
