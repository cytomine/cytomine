package be.cytomine.common.repository.model.stat.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

public record StatTerm(long id, String name, String color, long count) {

    @JsonInclude
    String key() {
        return name;
    }
}
