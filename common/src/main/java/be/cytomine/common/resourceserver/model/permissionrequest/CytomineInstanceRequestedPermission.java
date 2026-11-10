package be.cytomine.common.resourceserver.model.permissionrequest;

import be.cytomine.common.resourceserver.model.permission.CytomineInstancePermission;

public record CytomineInstanceRequestedPermission(CytomineInstancePermission permission
) implements CytomineRequestedPermission {
}
