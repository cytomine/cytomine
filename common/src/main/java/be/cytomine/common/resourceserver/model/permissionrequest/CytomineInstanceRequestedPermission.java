package be.cytomine.common.resourceserver.model.permissionrequest;

import be.cytomine.common.resourceserver.model.permission.CytomineInstancePermission;
import be.cytomine.common.resourceserver.model.permission.PermissionType;

public record CytomineInstanceRequestedPermission(CytomineInstancePermission permission)
    implements CytomineRequestedPermission {
    @Override
    public PermissionType permissionType() {
        return PermissionType.INSTANCE;
    }
}
