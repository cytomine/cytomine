package be.cytomine.common.resourceserver.model.permission;

public enum CytomineInstancePermission implements CytominePermission {
    CAN_CREATE_PROJECT, CAN_SEE_ALL_PROJECTS;


    @Override
    public PermissionType getPermissionType() {
        return PermissionType.INSTANCE;
    }
}
