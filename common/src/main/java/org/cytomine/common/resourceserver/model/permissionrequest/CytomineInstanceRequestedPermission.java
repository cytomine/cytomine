package org.cytomine.common.resourceserver.model.permissionrequest;

import org.cytomine.common.resourceserver.model.permission.CytomineInstancePermission;
import org.cytomine.common.resourceserver.model.permission.PermissionType;

public record CytomineInstanceRequestedPermission(CytomineInstancePermission permission)
    implements CytomineRequestedPermission {
    @Override
    public PermissionType permissionType() {
        return PermissionType.INSTANCE;
    }
}
