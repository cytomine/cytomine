package be.cytomine.common.repository.model.stat.payload;

import java.util.HashSet;
import java.util.Set;

public record StatUserTerm(long userId, String key, Set<StatTerm> terms) {
    public StatUserTerm {
        if (terms == null) {
            terms = new HashSet<>();
        }
    }
}
