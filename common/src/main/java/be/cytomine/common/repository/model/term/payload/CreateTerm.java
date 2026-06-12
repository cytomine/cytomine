package be.cytomine.common.repository.model.term.payload;

import java.util.Optional;

import jakarta.validation.constraints.NotEmpty;

public record CreateTerm(@NotEmpty String name, @NotEmpty String color, long ontology, Optional<String> comment) {


}
