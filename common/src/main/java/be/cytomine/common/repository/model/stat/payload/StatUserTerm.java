package be.cytomine.common.repository.model.stat.payload;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

public record StatUserTerm(long userId, String username, Set<StatTerm> terms) {
    public StatUserTerm {
        if (terms == null) {
            terms = new HashSet<>();
        }
    }

    @JsonInclude
    String key() {
        return username;
    }
}
