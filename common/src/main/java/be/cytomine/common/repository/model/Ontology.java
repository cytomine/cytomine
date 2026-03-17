package be.cytomine.common.repository.model;

import java.util.Set;

public record Ontology(long id, String name, long userId, Set<TermResponse> termResponses) {
    public Ontology {
        if (termResponses == null) {
            termResponses = Set.of();
        }
    }

}
