package org.cytomine.common.resourceserver.model;

import java.util.Set;

public record AuthorizationResponse(Set<CytomineGrantedPermission> grantedPermissions,
                                    Set<CytomineDeniedPermission> deniedPermissions) {
}
