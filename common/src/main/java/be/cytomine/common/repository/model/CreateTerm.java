package be.cytomine.common.repository.model;

import jakarta.validation.constraints.NotEmpty;

public record CreateTerm(@NotEmpty String name, @NotEmpty String color, long ontology,
                         String comment) {

}
