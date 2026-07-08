package be.cytomine.common.repository.model.role.payload;

import jakarta.validation.constraints.NotEmpty;

public record CreateRole(@NotEmpty String authority) {}
