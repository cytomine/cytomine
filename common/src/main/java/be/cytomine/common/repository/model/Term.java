package be.cytomine.common.repository.model;

import java.util.Set;

public record Term(long id, String name, String color, long ontologyId, long projectId, Set<Term> children) {
    public Term {
        if (children == null) {
            children = Set.of();
        }
    }
}
