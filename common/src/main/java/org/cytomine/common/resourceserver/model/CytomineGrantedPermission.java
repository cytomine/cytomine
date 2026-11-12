package org.cytomine.common.resourceserver.model;

import org.cytomine.common.resourceserver.model.permissionrequest.CytomineRequestedPermission;

public record CytomineGrantedPermission(CytomineRequestedPermission permission) {
}
