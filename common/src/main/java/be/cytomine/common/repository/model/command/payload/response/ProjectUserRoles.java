package be.cytomine.common.repository.model.command.payload.response;

public record ProjectUserRoles(
    boolean admin,
    boolean representative
) {}
