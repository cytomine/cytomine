package org.cytomine.e2etests.api;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakClient {

    @Value("${keycloak-client.url}")
    String url;

    @Value("${keycloak-client.admin.user}")
    String adminUser;

    @Value("${keycloak-client.admin.password}")
    String adminPassword;

    @Value("${keycloak-client.admin.realm}")
    String adminRealm;

    @Value("${keycloak-client.target.realm}")
    String targetRealm;

    @Value("${keycloak-client.client-id}")
    String clientId;

    public void deleteUser(String username) {
        try (Keycloak keycloak = Keycloak.getInstance(url, adminRealm, adminUser, adminPassword, clientId)) {
            keycloak.realm(targetRealm)
                .users()
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .map(UserRepresentation::getId)
                .ifPresent(id -> keycloak.realm(targetRealm).users().delete(id));
        }
    }
}
