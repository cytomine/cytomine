package org.cytomine.repository.service;

import java.util.Optional;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionsService {

    public static final String CLIENT_NAME = "core";
    public static final String RESOURCE_NAME = "core";
    private final Keycloak keycloak;
    private final ObjectMapper jsonMapper;

    @Value("${keycloak-client.target.realm}")
    String realm;

    @PostConstruct
    public void writeConfigToKeycloak() {
        RealmResource realmResource = keycloak.realm(realm);

        ClientsResource clients = realmResource.clients();

        ClientResource existingClient =
            clients.findByClientId(CLIENT_NAME).stream().findFirst().map(ignored -> clients.get(CLIENT_NAME))
                .orElseGet(() -> {
                    realmResource.clients().create(getBaseClientRepresentation(new ClientRepresentation()));
                    return realmResource.clients().get(CLIENT_NAME);
                });

    }


    <T> Optional<T> wrapException(Supplier<T> call) {
        try {
            T result = call.get();
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    ClientRepresentation getBaseClientRepresentation(ClientRepresentation clientRepresentation) {
        clientRepresentation.setId(CLIENT_NAME);
        clientRepresentation.setName(CLIENT_NAME);
        clientRepresentation.setAuthorizationServicesEnabled(true);
        clientRepresentation.setServiceAccountsEnabled(true);
        return clientRepresentation;
    }


}
