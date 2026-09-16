package org.cytomine.e2etests.utils;

import be.cytomine.common.repository.model.Role;

public record CreatedUser(Role role, String username, String password) {}
