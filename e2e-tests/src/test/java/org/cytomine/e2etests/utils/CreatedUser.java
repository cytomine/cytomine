package org.cytomine.e2etests.utils;

import org.cytomine.common.repository.model.Role;

public record CreatedUser(Role role,
                          String username,
                          String password
) {}
