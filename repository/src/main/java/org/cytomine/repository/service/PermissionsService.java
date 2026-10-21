package org.cytomine.repository.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cytomine.repository.config.CytominePermissions;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.ResourceResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.config.security.CytominePermission;

import static org.keycloak.representations.idm.authorization.DecisionStrategy.UNANIMOUS;

@Component
@RequiredArgsConstructor
public class PermissionsService {

    private final Keycloak keycloak;

    private final CytominePermissions cytominePermissions;

    private final ObjectMapper jsonMapper;

    @Value("${keycloak-client.target.realm}")
    String realm;

    @PostConstruct
    public void writePermissionsToKeycloak() {
        RealmResource realmResource = keycloak.realm(realm);

        ClientsResource clients = realmResource.clients();

        ClientResource existingClient = clients.findByClientId("cytomine-permissions").stream().findFirst()
            .map(ignored -> clients.get("cytomine-permissions")).orElseGet(() -> {
                    realmResource.clients()
                        .create(getBaseClientRepresentation(new ClientRepresentation()));
                    return realmResource.clients().get("cytomine-permissions");
                }
            );

        ResourceResource resourceResource =
            existingClient.authorization().resources().findByName("core").stream().findFirst()
                .map(ignored -> existingClient.authorization().resources().resource("core"))
                .orElseGet(() -> {
                    existingClient.authorization().resources().create(getResourceRepresentation());
                    return existingClient.authorization().resources().resource("core");
                });

        cytominePermissions.roles().forEach(
            (key, value) -> wrapException(() -> existingClient.roles().get(key).toRepresentation()).map(ignored -> true)
                .orElseGet(() -> {
                    existingClient.roles().create(getRoleRepresentation(key));
                    return true;
                }));

        existingClient.authorization()
            .importSettings(getResourceServerRepresentation(cytominePermissions));
    }

    <T> Optional<T> wrapException(Supplier<T> call) {
        try {
            T result = call.get();
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    RoleRepresentation getRoleRepresentation(String role) {
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setId(role);
        roleRepresentation.setName(role);
        roleRepresentation.setClientRole(true);
        return roleRepresentation;
    }

    ClientRepresentation getBaseClientRepresentation(ClientRepresentation clientRepresentation) {
        clientRepresentation.setId("cytomine-permissions");
        clientRepresentation.setName("cytomine-permissions");
        clientRepresentation.setAuthorizationServicesEnabled(true);
        clientRepresentation.setServiceAccountsEnabled(true);
        return clientRepresentation;
    }

    ResourceServerRepresentation getResourceServerRepresentation(CytominePermissions cytominePermissions) {
        ResourceServerRepresentation resourceServerRepresentation = new ResourceServerRepresentation();

        resourceServerRepresentation.setScopes(
            Stream.of(CytominePermission.values()).map(this::getScopeRepresentation).toList());
        resourceServerRepresentation.setPolicies(cytominePermissions.roles().entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .flatMap(v1 -> wrapException(() -> getPolicyRepresentation(entry.getKey(), v1)).stream()))
            .toList());
        return resourceServerRepresentation;
    }

    ScopeRepresentation getScopeRepresentation(CytominePermission scope) {
        ScopeRepresentation scopeRepresentation = new ScopeRepresentation();
        scopeRepresentation.setId(scope.toString());
        scopeRepresentation.setName(scope.toString());
        scopeRepresentation.setResources(List.of(getResourceRepresentation()));
        return scopeRepresentation;
    }

    @SneakyThrows
    PolicyRepresentation getPolicyRepresentation(String role, CytominePermission policy) {
        PolicyRepresentation policyRepresentation = new PolicyRepresentation();
        policyRepresentation.setId(role + "." + policy);
        policyRepresentation.setName(role + "." + policy);
        policyRepresentation.setType("role");
        policyRepresentation.setScopes(Set.of(policy.toString()));
        policyRepresentation.setDecisionStrategy(UNANIMOUS);
        policyRepresentation.setLogic(Logic.POSITIVE);
        policyRepresentation.setConfig(
            Map.of("roles", jsonMapper.writeValueAsString(List.of(new SimpleRoleRepresentation(role, false)))));
        return policyRepresentation;

    }

    ResourceRepresentation getResourceRepresentation() {
        ResourceRepresentation resourceRepresentation = new ResourceRepresentation();
        resourceRepresentation.setId("core");
        resourceRepresentation.setName("core");

        return resourceRepresentation;
    }

}
