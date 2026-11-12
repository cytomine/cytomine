package org.cytomine.authorizationapi.config;

import java.util.Arrays;

import org.cytomine.common.authorizationapi.model.permission.CytomineInstancePermission;
import org.cytomine.common.authorizationapi.model.permission.CytominePermission;
import org.cytomine.common.authorizationapi.model.permission.CytomineProjectPermission;
import org.cytomine.common.authorizationapi.model.permissionconfig.CytominePermissionConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionsConfigServiceTest {

    final PermissionsConfigService permissionsConfigService = new PermissionsConfigService();

    /**
     * This method is here only to validate that all enums in CytominePermission are in CytominePermissionConfig.
     */
    public CytominePermissionConfig mapRevert(CytominePermission cytominePermission) {
        return switch (cytominePermission) {
            case CytomineInstancePermission cytomineInstancePermission -> switch (cytomineInstancePermission) {
                case CAN_CREATE_PROJECT -> CytominePermissionConfig.CAN_CREATE_PROJECT;
                case CAN_SEE_ALL_PROJECTS -> CytominePermissionConfig.CAN_SEE_ALL_PROJECTS;
            };
            case CytomineProjectPermission cytomineProjectPermission -> switch (cytomineProjectPermission) {
                case PROJECT_OWNER_CAN_ADD_MEMBERS -> CytominePermissionConfig.PROJECT_OWNER_CAN_ADD_MEMBERS;
            };
        };
    }

    @Test
    void uselessTestToValidateBijection() {
        Arrays.stream(CytominePermissionConfig.values())
            .forEach(cpc -> assertEquals(cpc, mapRevert(permissionsConfigService.map(cpc))));
    }

}
