package be.cytomine.common.resourceserver.model.permission;

import static be.cytomine.common.resourceserver.model.permission.PermissionType.PROJECT;

public enum CytomineProjectPermission implements CytominePermission {
    PROJECT_OWNER_CAN_ADD_MEMBERS;

    @Override
    public PermissionType getPermissionType() {
        return PROJECT;
    }
}
