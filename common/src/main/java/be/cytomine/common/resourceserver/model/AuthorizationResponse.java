package be.cytomine.common.resourceserver.model;

import java.util.Set;

import be.cytomine.common.config.security.CytomineDeniedPermissions;
import be.cytomine.common.config.security.CytomineGrantedPermission;

public record AuthorizationResponse(Set<CytomineGrantedPermission> grantedPermissions,
                                    Set<CytomineDeniedPermissions> deniedPermissions) {
}
