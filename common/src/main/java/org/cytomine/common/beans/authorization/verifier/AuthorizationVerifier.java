package org.cytomine.common.beans.authorization.verifier;

import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.cytomine.common.authorizationapi.model.CytomineGrantedPermission;
import org.cytomine.common.authorizationapi.service.PermissionsServiceV2;
import org.cytomine.common.exceptions.ForbiddenException;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@RequiredArgsConstructor
@Component
public class AuthorizationVerifier {

    private final PermissionsServiceV2 permissionsServiceV2;

    public void authorize(AuthorizationRequest authorizationRequest) {
        if (authorizationRequest.permissionsRequested().isEmpty()) {
            return;
        }
        AuthorizationResponse authorize = permissionsServiceV2.authorize(authorizationRequest);
        if (!verifyGranted(authorizationRequest, authorize)) {
            throw new ForbiddenException(format("Missing permissions: %s", authorize.deniedPermissions()));
        }

    }

    boolean verifyGranted(AuthorizationRequest authorizationRequest, AuthorizationResponse authorizationResponse) {
        return authorizationRequest.permissionsRequested().equals(
            authorizationResponse.grantedPermissions().stream().map(CytomineGrantedPermission::permission)
                .collect(Collectors.toSet()));
    }
}
