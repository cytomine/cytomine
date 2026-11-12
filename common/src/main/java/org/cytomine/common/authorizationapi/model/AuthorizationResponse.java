package org.cytomine.common.authorizationapi.model;

import java.util.Set;

public record AuthorizationResponse(Set<CytomineGrantedPermission> grantedPermissions,
                                    Set<CytomineDeniedPermission> deniedPermissions) {
}
