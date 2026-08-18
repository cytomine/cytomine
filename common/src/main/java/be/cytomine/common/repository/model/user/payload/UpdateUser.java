package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;


public record UpdateUser(Optional<String> email, Optional<String> name, Optional<String> firstname,
                         Optional<String> lastname, Optional<String> language, Optional<String> origin,
                         Optional<Boolean> developer, Optional<String> privateKey, Optional<String> publicKey,
                         Optional<String> role, Optional<String> password, String username) {}
