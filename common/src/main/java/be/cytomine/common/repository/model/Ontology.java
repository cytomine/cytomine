package be.cytomine.common.repository.model;

import java.util.Set;

public record Ontology(long id, String name, long userId, Set<TermResponse> terms) {
    public Ontology {
        if (terms == null) {
            terms = Set.of();
        }
    }

}
