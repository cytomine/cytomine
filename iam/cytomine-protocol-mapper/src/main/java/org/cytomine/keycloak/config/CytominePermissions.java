package org.cytomine.keycloak.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import be.cytomine.common.config.security.CytominePermission;

public record CytominePermissions(
    Map<String, Set<CytominePermission>> roles, Map<String, Set<CytominePermission>> projectRoles
) {
}
