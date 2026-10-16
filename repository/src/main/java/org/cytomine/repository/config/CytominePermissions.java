package org.cytomine.repository.config;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import be.cytomine.common.config.security.CytominePermission;

@ConfigurationProperties(prefix = "cytomine.permissions")
public record CytominePermissions(
    Map<String, Set<CytominePermission>> roles, Map<String, Set<CytominePermission>> projectRoles
) {
}
