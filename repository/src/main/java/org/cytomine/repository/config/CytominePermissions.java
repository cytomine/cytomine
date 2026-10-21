package org.cytomine.repository.config;

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
}
