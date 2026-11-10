package org.cytomine.common.resourceserver.model.permissionrequest;

import org.cytomine.common.resourceserver.model.permission.CytomineProjectPermission;
import org.cytomine.common.resourceserver.model.permission.PermissionType;

public record CytomineProjectRequestedPermission(CytomineProjectPermission permission, long id)
    implements CytomineRequestedPermission {

    @Override
    public PermissionType permissionType() {
        return PermissionType.PROJECT;
    }
}
