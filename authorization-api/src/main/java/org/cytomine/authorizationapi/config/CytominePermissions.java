package org.cytomine.authorizationapi.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytomine.common.authorizationapi.model.permissionconfig.CytominePermissionConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "cytomine.permissions")
public record CytominePermissions(
    Map<String, Set<CytominePermissionConfig>> roles,
    Map<String, Set<CytominePermissionConfig>> projectRoles

) {
    public CytominePermissions {
        if (roles == null) {
            roles = new HashMap<>();
        }
        if (projectRoles == null) {
            projectRoles = new HashMap<>();
        }

    }
}
