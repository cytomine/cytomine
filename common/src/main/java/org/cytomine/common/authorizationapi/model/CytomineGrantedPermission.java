package org.cytomine.common.authorizationapi.model;

import org.cytomine.common.authorizationapi.model.permissionrequest.CytomineRequestedPermission;

public record CytomineGrantedPermission(CytomineRequestedPermission permission) {
}
