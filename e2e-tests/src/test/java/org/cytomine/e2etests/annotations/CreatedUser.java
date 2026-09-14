package org.cytomine.e2etests.annotations;

import be.cytomine.common.repository.model.Role;

public record CreatedUser(Role role, String username, String password) {}
