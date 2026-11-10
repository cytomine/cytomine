package org.cytomine.resourceserver.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import be.cytomine.common.resourceserver.model.permission.CytominePermission;


@ConfigurationProperties(prefix = "cytomine.permissions")
public record CytominePermissions(
    Map<String, Set<CytominePermission>> roles,
    Map<String, Set<CytominePermission>> projectRoles

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
