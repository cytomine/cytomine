package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.command.payload.response.RoleResponse;

public record UpdateUser(Optional<String> email, Optional<String> name,
                         Optional<String> firstname, Optional<String> lastname, Optional<String> language,
                         Optional<String> origin, Optional<Boolean> developer,Optional<String> privateKey,
                         Optional<String> publicKey, Optional<Set<RoleResponse>> roles) {
}
