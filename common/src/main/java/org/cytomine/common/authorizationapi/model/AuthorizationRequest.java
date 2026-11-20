package org.cytomine.common.authorizationapi.model;

import java.util.Set;

import org.cytomine.common.authorizationapi.model.permissionrequest.CytomineRequestedPermission;

public record AuthorizationRequest(Set<CytomineRequestedPermission> permissionsRequested) {
}
