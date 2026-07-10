package org.cytomine.repository.service;

import java.util.Arrays;
import java.util.Set;

import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.Role;

import static java.util.stream.Collectors.toSet;

@Component
public class RoleHierarchy {

    public Set<Role> getRolesUpTo(Role role) {
        return Arrays.stream(Role.values()).filter(r -> r.ordinal() <= role.ordinal()).collect(toSet());
    }
}
