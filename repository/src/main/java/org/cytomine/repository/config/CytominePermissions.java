package org.cytomine.repository.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import be.cytomine.common.config.security.CytominePermission;
import be.cytomine.common.config.security.CytomineUserRole;

@ConfigurationProperties(prefix = "cytomine.permissions")
public record CytominePermissions(
    Map<String, Set<CytominePermission>> roles,
    Map<String, Set<CytominePermission>> projectRoles,
    Set<CytomineUserRole> users
) {
    public CytominePermissions {
        if (roles == null) {
            roles = new HashMap<>();
        }
        if (projectRoles == null) {
            projectRoles = new HashMap<>();
        }
        if (users == null) {
            users = new HashSet<>();
        }
    }
}
