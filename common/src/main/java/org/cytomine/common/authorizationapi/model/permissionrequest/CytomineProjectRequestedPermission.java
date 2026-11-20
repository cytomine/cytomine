package org.cytomine.common.authorizationapi.model.permissionrequest;

import org.cytomine.common.authorizationapi.model.permission.CytomineProjectPermission;
import org.cytomine.common.authorizationapi.model.permission.PermissionType;

public record CytomineProjectRequestedPermission(CytomineProjectPermission permission, long id)
    implements CytomineRequestedPermission {

    @Override
    public PermissionType permissionType() {
        return PermissionType.PROJECT;
    }
}
