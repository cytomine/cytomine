package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;

public record CreateUser(String username,
                         Optional<String> firstname,
                         Optional<String> lastname,
                         String email,
                         Optional<String> language) {}
