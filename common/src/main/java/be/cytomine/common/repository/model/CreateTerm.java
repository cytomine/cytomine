package be.cytomine.common.repository.model;

import java.util.Date;

import jakarta.validation.constraints.NotEmpty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateTerm(@NotEmpty String name, @NotEmpty String color, long ontology,
                         @NotEmpty Date created,
                         @NotEmpty Date updated,
                         String comment) {

}
