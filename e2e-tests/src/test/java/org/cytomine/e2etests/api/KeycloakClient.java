package org.cytomine.e2etests.api;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakClient {

    @Value("${keycloak-client.url}")
    String url;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    private static final String REALM = "cytomine";
    private static final String CLIENT_ID = "admin-cli";

    public void deleteUser(String username) {
        try (Keycloak keycloak = Keycloak.getInstance(url, REALM, adminUsername, adminPassword, CLIENT_ID)) {
            keycloak.realm(REALM)
                .users()
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .map(UserRepresentation::getId)
                .ifPresent(id -> keycloak.realm(REALM).users().delete(id));
        }
    }
}
