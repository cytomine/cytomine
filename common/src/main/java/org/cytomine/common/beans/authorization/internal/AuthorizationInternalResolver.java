package org.cytomine.common.beans.authorization.internal;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cytomine.common.authorizationapi.model.AuthorizationRequest;
import org.cytomine.common.authorizationapi.model.AuthorizationResponse;
import org.cytomine.common.authorizationapi.model.CytomineDeniedPermission;
import org.cytomine.common.authorizationapi.model.CytomineGrantedPermission;
import org.cytomine.common.authorizationapi.model.permission.CytominePermission;
import org.cytomine.common.authorizationapi.service.PermissionsServiceV2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthorizationInternalResolver implements PermissionsServiceV2 {

    private final CytominePermissions cytominePermissions;
    private final PermissionsConfigService permissionsConfigService;

    @PostConstruct
    public void printPermissions() {
        log.info("{}", cytominePermissions);
    }

    @Override
    public AuthorizationResponse authorize(@RequestBody AuthorizationRequest authorizationRequest) {
        Set<String> roles =
            SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::toString)
                .filter(ga -> ga.startsWith("ROLE_"))
                .map(r -> r.substring(5))
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        log.info("Roles for user {}", roles);
        Set<CytominePermission> permissions =
            roles.stream().flatMap(r -> cytominePermissions.roles().entrySet().stream()
                    .filter(entry -> entry.getKey().toUpperCase().equals(r)))
                .flatMap(e -> e.getValue().stream())
                .map(permissionsConfigService::map)
                .collect(
                    Collectors.toSet());
        log.info("Computed permissions for user: {}", permissions);
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
