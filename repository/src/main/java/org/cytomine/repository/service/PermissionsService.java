package org.cytomine.repository.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.config.CytominePermissions;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionsService {

    private final Keycloak keycloak;

    private final CytominePermissions cytominePermissions;
    @Value("${keycloak-client.admin.realm}")
    String realm;

    @PostConstruct
    public void writePermissionsToKeycloak() {
        RealmResource realmResource = keycloak.realm(realm);

        realmResource.clientPoliciesPoliciesResource();
        ClientRepresentation client =
            realmResource.clients().findByClientId("cytomine-permissions").stream().findFirst()
                .orElseGet(() -> realmResource.clients().create(new ClientRepresentation()).readEntity(
                    ClientRepresentation.class));

        System.out.println(cytominePermissions);
        // cytominePermissions.roles().


    }

    ClientRepresentation getClientRepresentation() {
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId("cytomine-permissions");
        clientRepresentation.setAuth
        return clientRepresentation;
    }

    void createPolicy() {}


}
