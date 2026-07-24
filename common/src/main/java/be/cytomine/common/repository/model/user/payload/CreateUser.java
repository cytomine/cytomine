package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;

public record CreateUser(String username, Optional<String> name, Optional<String> firstname, Optional<String> lastname,
                         String email, Optional<String> origin, boolean developer, String role, String language,
                         Optional<String> privateKey, Optional<String> publicKey, String password) {}
