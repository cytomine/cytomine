package org.cytomine.common.beans.authorization.verifier;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.cytomine.common.authorizationapi.model.CytomineGrantedPermission;
import org.cytomine.common.authorizationapi.model.permission.CytomineInstancePermission;
import org.cytomine.common.authorizationapi.model.permissionrequest.CytomineInstanceRequestedPermission;
import org.cytomine.common.authorizationapi.service.PermissionsServiceV2;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthorizationVerifier {

    private final PermissionsServiceV2 permissionsServiceV2;

    public boolean authorize(AuthorizationRequest authorizationRequest) {
        return verifyGranted(authorizationRequest, permissionsServiceV2.authorize(authorizationRequest));
    }

    boolean verifyGranted(AuthorizationRequest authorizationRequest,
        AuthorizationResponse authorizationResponse) {
        return authorizationRequest.permissionsRequested()
            .equals(authorizationResponse.grantedPermissions().stream().map(CytomineGrantedPermission::permission)
                .collect(Collectors.toSet()));
    }

    public boolean hasInstancePermission(String cytomineInstancePermission) {
        return CytomineInstancePermission.fromString(cytomineInstancePermission).map(permission -> {
            AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(Set.of(new CytomineInstanceRequestedPermission(permission)));
            return verifyGranted(authorizationRequest, permissionsServiceV2.authorize(authorizationRequest));
        }).orElse(false);
    }

}
