package be.cytomine.common.repository.model.user.payload;

import java.util.Optional;

public record UpdateUser(Optional<String> username,
                         Optional<String> email) {}
