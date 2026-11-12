package org.cytomine.common.authorizationapi.model;

import org.cytomine.common.authorizationapi.model.permissionrequest.CytomineRequestedPermission;

public record CytomineDeniedPermission(CytomineRequestedPermission requestedPermission, String reason) {
}
