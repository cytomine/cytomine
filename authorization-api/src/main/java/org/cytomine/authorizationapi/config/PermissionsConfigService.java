package org.cytomine.authorizationapi.config;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.authorizationapi.model.permission.CytomineInstancePermission;
import org.cytomine.common.authorizationapi.model.permission.CytominePermission;
import org.cytomine.common.authorizationapi.model.permission.CytomineProjectPermission;
import org.cytomine.common.authorizationapi.model.permissionconfig.CytominePermissionConfig;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PermissionsConfigService {

    public CytominePermission map(CytominePermissionConfig cytominePermissionConfig) {
        return switch (cytominePermissionConfig) {
            case CytominePermissionConfig.CAN_CREATE_PROJECT -> CytomineInstancePermission.CAN_CREATE_PROJECT;
            case CytominePermissionConfig.CAN_SEE_ALL_PROJECTS -> CytomineInstancePermission.CAN_SEE_ALL_PROJECTS;
            case CytominePermissionConfig.PROJECT_OWNER_CAN_ADD_MEMBERS ->
                CytomineProjectPermission.PROJECT_OWNER_CAN_ADD_MEMBERS;
        };
    }


}
