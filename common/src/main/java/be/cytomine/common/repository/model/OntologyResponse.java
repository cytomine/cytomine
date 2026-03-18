package be.cytomine.common.repository.model;

import java.util.Set;

public record OntologyResponse(long id, String name, long userId,
                               Set<TermResponse> terms) {
    public OntologyResponse {
        if (terms == null) {
            terms = Set.of();


        }

    }
}
