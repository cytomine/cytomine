package be.cytomine.common.repository.model;

import java.util.Date;
import java.util.Set;

public record TermResponse(long id, String name, String color, long ontologyId, long ontology,
                           Date created, Date updated,
                           String comment, Set<TermResponse> children) {
    public TermResponse {
        if (children == null) {
            children = Set.of();
        }
    }
}
