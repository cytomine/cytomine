package org.cytomine.common.authorizationapi.model.permissionrequest;

import org.cytomine.common.authorizationapi.model.permission.CytomineInstancePermission;
import org.cytomine.common.authorizationapi.model.permission.PermissionType;

public record CytomineInstanceRequestedPermission(CytomineInstancePermission permission)
    implements CytomineRequestedPermission {
    @Override
    public PermissionType permissionType() {
        return PermissionType.INSTANCE;
    }
}
