package be.cytomine.common.repository.model.term.payload;

import jakarta.validation.constraints.NotEmpty;

public record CreateTerm(@NotEmpty String name, @NotEmpty String color, long ontology,
                         String comment) {

}
