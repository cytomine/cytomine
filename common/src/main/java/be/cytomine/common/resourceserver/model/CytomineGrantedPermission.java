package be.cytomine.common.resourceserver.model;

import be.cytomine.common.resourceserver.model.permissionrequest.CytomineRequestedPermission;

public record CytomineGrantedPermission(CytomineRequestedPermission permission) {
}
