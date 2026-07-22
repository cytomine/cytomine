package be.cytomine.common.repository.model.tag.payload;

import jakarta.validation.constraints.NotEmpty;

public record CreateTag(@NotEmpty String name) {}
