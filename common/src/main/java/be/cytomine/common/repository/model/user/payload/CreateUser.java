package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CreateUser(String username, Optional<String> name, Optional<String> firstname, Optional<String> lastname,
                         String email, Optional<String> origin, @JsonAlias("isDeveloper") boolean developer,
                         String role, String language, Optional<String> privateKey, Optional<String> publicKey,
                         String password, Optional<String> reference) {}
