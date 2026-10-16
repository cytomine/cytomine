package org.cytomine.repository.service;

import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.config.CytominePermissions;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.cytomine.common.config.security.CytominePermission;

@Component
@RequiredArgsConstructor
public class PermissionsService {

    private final Keycloak keycloak;

    private final CytominePermissions cytominePermissions;
    @Value("${keycloak-client.target.realm}")
    String realm;

    @PostConstruct
    public void writePermissionsToKeycloak() {
        RealmResource realmResource = keycloak.realm(realm);
        realmResource.update(getRealmRepresentation(realmResource.toRepresentation(), cytominePermissions));
    }

    private Response wrapResponse(Response response) {
        if (response.getStatus() != 200) {
            throw new RuntimeException();
        }
        return response;
    }

    RealmRepresentation getRealmRepresentation(RealmRepresentation realmRepresentation,
        CytominePermissions cytominePermissions) {
        realmRepresentation.setClients(
            realmRepresentation.getClients().stream().map(clientRepresentation ->
                {
                    if (clientRepresentation.getId().equals("cytomine-permissions")) {
                        return getClientRepresentation(cytominePermissions);
                    } else {
                        return clientRepresentation;
                    }
                }
            ).toList());
        return realmRepresentation;
    }

    ClientRepresentation getClientRepresentation(CytominePermissions cytominePermissions) {
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId("cytomine-permissions");
        clientRepresentation.setAuthorizationServicesEnabled(true);
        clientRepresentation.setServiceAccountsEnabled(true);
        clientRepresentation.setAuthorizationSettings(getResourceServerRepresentation(cytominePermissions));
        return clientRepresentation;
    }

    ResourceServerRepresentation getResourceServerRepresentation(CytominePermissions cytominePermissions) {
        ResourceServerRepresentation resourceServerRepresentation = new ResourceServerRepresentation();
        resourceServerRepresentation.setScopes(
            Stream.of(CytominePermission.values()).map(this::getScopeRepresentation).toList());
        cytominePermissions.roles().forEach((roleName, permissions) -> {

        });
        return resourceServerRepresentation;
    }

    ScopeRepresentation getScopeRepresentation(CytominePermission scope) {
        ScopeRepresentation scopeRepresentation = new ScopeRepresentation();
        scopeRepresentation.setId(scope.toString());
        return scopeRepresentation;


    }


}
