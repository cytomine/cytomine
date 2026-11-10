package be.cytomine.common.resourceserver.model.permissionrequest;

import be.cytomine.common.resourceserver.model.permission.CytominePermission;

public sealed interface CytomineRequestedPermission
    permits CytomineInstanceRequestedPermission, CytomineProjectRequestedPermission {
    public CytominePermission permission();
}
