package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAlias;


public record UpdateUser(Optional<String> email, Optional<String> name, Optional<String> firstname,
                         Optional<String> lastname, Optional<String> language, Optional<String> origin,
                         @JsonAlias("isDeveloper") Optional<Boolean> developer, Optional<String> privateKey,
                         Optional<String> publicKey, Optional<String> role, Optional<String> password,
                         String username) {}
