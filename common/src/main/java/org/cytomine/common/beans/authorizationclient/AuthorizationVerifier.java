package org.cytomine.common.beans.authorizationclient;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.authorizationapi.http.PermissionsHttpContract;
import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.cytomine.common.authorizationapi.model.CytomineGrantedPermission;
import org.cytomine.common.authorizationapi.model.permission.CytomineInstancePermission;
import org.cytomine.common.authorizationapi.model.permissionrequest.CytomineInstanceRequestedPermission;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthorizationVerifier {

    private final PermissionsHttpContract authorizationRestClient;

    private boolean verifyGranted(AuthorizationRequest authorizationRequest,
        AuthorizationResponse authorizationResponse) {
        return authorizationRequest.permissionsRequested()
            .equals(authorizationResponse.grantedPermissions().stream().map(CytomineGrantedPermission::permission));
    }

    public boolean hasInstancePermission(String cytomineInstancePermission) {
        return CytomineInstancePermission.fromString(cytomineInstancePermission).map(permission -> {
            AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(Set.of(new CytomineInstanceRequestedPermission(permission)));
            return verifyGranted(authorizationRequest, authorizationRestClient.authorize(authorizationRequest));
        }).orElse(false);
    }

}
