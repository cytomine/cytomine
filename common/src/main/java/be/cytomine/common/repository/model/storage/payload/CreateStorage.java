package be.cytomine.common.repository.model.storage.payload;

import jakarta.validation.constraints.NotEmpty;

public record CreateStorage(@NotEmpty String name) {}
