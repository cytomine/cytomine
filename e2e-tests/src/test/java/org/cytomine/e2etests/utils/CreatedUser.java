package org.cytomine.e2etests.utils;

import java.util.Set;

import be.cytomine.common.repository.model.Role;

public record CreatedUser(Role role,
                          String username,
                          String password,
                          Set<String> projectNames) {}
