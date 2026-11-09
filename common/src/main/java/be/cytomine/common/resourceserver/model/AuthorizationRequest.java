package be.cytomine.common.resourceserver.model;

import java.util.Set;

import be.cytomine.common.config.security.CytominePermission;
import be.cytomine.common.config.security.CytomineRequestedPermission;

public record AuthorizationRequest(Set<CytomineRequestedPermission> permissionsRequested) {
}
