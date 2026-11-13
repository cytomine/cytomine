package org.cytomine.authorizationapi.controller;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.cytomine.authorizationapi.config.CytominePermissions;
import org.cytomine.authorizationapi.config.PermissionsConfigService;
import org.cytomine.common.authorizationapi.http.PermissionsHttpContract;
import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.cytomine.common.authorizationapi.model.CytomineDeniedPermission;
import org.cytomine.common.authorizationapi.model.CytomineGrantedPermission;
import org.cytomine.common.authorizationapi.model.permission.CytominePermission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController(PermissionsHttpContract.ROOT_PATH)
public class AuthorizationService implements PermissionsHttpContract {

    private final CytominePermissions cytominePermissions;
    private final PermissionsConfigService permissionsConfigService;

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
                .map(permissionsConfigService::map)
                .collect(
                    Collectors.toSet());

        return new AuthorizationResponse(
            authorizationRequest.permissionsRequested().stream()
                .filter(p -> permissions.contains(p.permission()))
                .map(CytomineGrantedPermission::new)
                .collect(Collectors.toSet()),
            authorizationRequest.permissionsRequested().stream()
                .filter(p -> !permissions.contains(p.permission()))
                .map(r -> new CytomineDeniedPermission(r, "Permission missing."))
                .collect(Collectors.toSet()));
    }
}
