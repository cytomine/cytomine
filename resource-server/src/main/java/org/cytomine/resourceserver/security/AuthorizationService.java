package org.cytomine.resourceserver.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.resourceserver.http.PermissionsHttpContract;
import be.cytomine.common.resourceserver.model.AuthorizationRequest;
import be.cytomine.common.resourceserver.model.AuthorizationResponse;
import be.cytomine.common.resourceserver.model.CytomineGrantedPermission;

@RestController(PermissionsHttpContract.ROOT_PATH)
public class AuthorizationService implements PermissionsHttpContract {
    public AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new AuthorizationResponse(authorizationRequest.permissionsRequested().stream()
            .map(r -> new CytomineGrantedPermission(r.permission(), r.targetResource()))
            .collect(Collectors.toSet()), Set.of());
    }
}
