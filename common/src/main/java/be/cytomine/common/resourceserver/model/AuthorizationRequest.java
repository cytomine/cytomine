package be.cytomine.common.resourceserver.model;

import java.util.Set;

import be.cytomine.common.resourceserver.model.permissionrequest.CytomineRequestedPermission;

public record AuthorizationRequest(Set<CytomineRequestedPermission> permissionsRequested) {
}
