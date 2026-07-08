package be.cytomine.common.repository.model.user.payload;

import java.util.Locale;
import java.util.Optional;

public record CreateUser(String username,
                         Optional<String> name,
                         Optional<String> firstname,
                         Optional<String> lastname,
                         String email,
                         Optional<String> locale) {}
