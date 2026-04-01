package be.cytomine.common.repository.model;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

public record TermResponse(long id, String name, String color, long ontologyId,
                           ZonedDateTime created, ZonedDateTime updated, Optional<ZonedDateTime> deleted,
                           String comment, Set<TermResponse> children) {
    public TermResponse {
        if (children == null) {
            children = Set.of();
        }
    }
}
