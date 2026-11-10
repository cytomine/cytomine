package be.cytomine.common.resourceserver.model;

import be.cytomine.common.resourceserver.model.permissionrequest.CytomineRequestedPermission;
import be.cytomine.common.resourceserver.model.target.TargetResource;

public record CytomineGrantedPermission(CytomineRequestedPermission permission) {
}
