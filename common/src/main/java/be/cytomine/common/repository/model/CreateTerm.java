package be.cytomine.common.repository.model;

import java.util.Date;
import java.util.Optional;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateTerm(@NotEmpty String name, @NotEmpty String color, long ontology,
                         String comment) {

}
