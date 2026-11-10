package org.cytomine.resourceserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.cytomine.common.resourceserver.model.permission.CytomineInstancePermission;
import org.cytomine.common.resourceserver.model.permission.CytominePermission;
import org.cytomine.common.resourceserver.model.permission.CytomineProjectPermission;
import org.cytomine.common.resourceserver.model.permissionconfig.CytominePermissionConfig;

@RequiredArgsConstructor
@Component
public class PermissionsConfigService {

    public CytominePermission map(CytominePermissionConfig cytominePermissionConfig) {
        return switch (cytominePermissionConfig) {
            case CAN_CREATE_PROJECT -> CytomineInstancePermission.CAN_CREATE_PROJECT;
            case CAN_SEE_ALL_PROJECTS -> CytomineInstancePermission.CAN_SEE_ALL_PROJECTS;
            case PROJECT_OWNER_CAN_ADD_MEMBERS -> CytomineProjectPermission.PROJECT_OWNER_CAN_ADD_MEMBERS;
        };
    }

}
