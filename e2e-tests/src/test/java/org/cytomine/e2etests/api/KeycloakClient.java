package org.cytomine.e2etests.api;

import java.util.Optional;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakClient {

    private static final String REALM = "cytomine";
    private static final String CLIENT_ID = "admin-cli";

    @Value("${keycloak-client.url}")
    String url;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    public void deleteUser(String username) {
        try (Keycloak keycloak = keycloak()) {
            findUser(keycloak, username)
                .map(UserRepresentation::getId)
                .ifPresent(id -> keycloak.realm(REALM).users().delete(id));
        }
    }

    public Optional<String> findUserId(String username) {
        try (Keycloak keycloak = keycloak()) {
            return findUser(keycloak, username).map(UserRepresentation::getId);
        }
    }

    private Optional<UserRepresentation> findUser(Keycloak keycloak, String username) {
        return keycloak.realm(REALM)
            .users()
            .searchByUsername(username, true)
            .stream()
            .findFirst();
    }

    private Keycloak keycloak() {
        return Keycloak.getInstance(url, REALM, adminUsername, adminPassword, CLIENT_ID);
    }
}
