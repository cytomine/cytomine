package be.cytomine.common.repository.model.ontology.payload;

import jakarta.validation.constraints.NotEmpty;

public record CreateOntology(@NotEmpty String name) {

}
