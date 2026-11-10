package org.cytomine.resourceserver.controller;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.cytomine.resourceserver.config.CytominePermissions;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.resourceserver.http.PermissionsHttpContract;
import be.cytomine.common.resourceserver.model.AuthorizationRequest;
import be.cytomine.common.resourceserver.model.AuthorizationResponse;
import be.cytomine.common.resourceserver.model.CytomineDeniedPermission;
import be.cytomine.common.resourceserver.model.CytomineGrantedPermission;
import be.cytomine.common.resourceserver.model.permission.CytominePermission;


@RequiredArgsConstructor
@RestController(PermissionsHttpContract.ROOT_PATH)
public class AuthorizationService implements PermissionsHttpContract {

    private final CytominePermissions cytominePermissions;

    public AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest) {
        Set<String> roles =
            SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::toString)
                .filter(ga -> ga.startsWith("ROLE_"))
                .map(r -> r.substring(5))
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        Set<CytominePermission> permissions =
            roles.stream().flatMap(r -> cytominePermissions.roles().entrySet().stream()
                    .filter(entry -> entry.getKey().toUpperCase().equals(r)))
                .flatMap(e -> e.getValue().stream())
                .collect(
                    Collectors.toSet());

        return new AuthorizationResponse(
            authorizationRequest.permissionsRequested().stream()
                .filter(p -> permissions.contains(p.permission()))
                .map(r -> new CytomineGrantedPermission(r))
                .collect(Collectors.toSet()),
            authorizationRequest.permissionsRequested().stream()
                .filter(p -> !permissions.contains(p.permission()))
                .map(r -> new CytomineDeniedPermission(r, "Permission missing."))
                .collect(Collectors.toSet()));
    }
}
