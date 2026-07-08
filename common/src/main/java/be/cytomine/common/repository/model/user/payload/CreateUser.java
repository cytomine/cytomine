package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.command.payload.response.RoleResponse;

public record CreateUser(String username,
                         Optional<String> name,
                         Optional<String> firstname,
                         Optional<String> lastname,
                         String email,
                         Optional<String> origin,
                         boolean developer,
                         Set<RoleResponse> roles,
                         String language) {
}
