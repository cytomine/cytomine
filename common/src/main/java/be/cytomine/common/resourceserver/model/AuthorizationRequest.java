package be.cytomine.common.resourceserver.model;

import java.util.Set;

public record AuthorizationRequest(Set<CytomineRequestedPermission> permissionsRequested) {
}
