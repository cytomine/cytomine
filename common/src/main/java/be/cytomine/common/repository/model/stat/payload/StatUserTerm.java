package be.cytomine.common.repository.model.stat.payload;

import java.util.ArrayList;
import java.util.List;

public record StatUserTerm(long id, String key, List<StatTerm> terms) {
    public StatUserTerm {
        if (terms == null) {
            terms = new ArrayList<>();
        }
    }
}
