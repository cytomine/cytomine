package org.cytomine.repository.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cytomine.repository.config.CytominePermissions;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
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
        getRealmRepresentation(realmResource, cytominePermissions);
    }

    void getRealmRepresentation(RealmResource realmResource, CytominePermissions cytominePermissions) {
        RealmRepresentation realmRepresentation = realmResource.toRepresentation();

        ClientsResource clients = realmResource.clients();


        clients.findByClientId("cytomine-permissions").stream().findFirst().ifPresentOrElse(clientRepresentation -> {
            ClientResource existingClient = clients.get("cytomine-permissions");
            existingClient.update(getClientRepresentation(clientRepresentation, cytominePermissions));
        }, () -> {
            cytominePermissions.roles()
                .forEach((key, value) ->
                    Optional.ofNullable(realmResource.roles().get(key)).map(ignored -> true).orElseGet(() -> {
                        realmResource.roles().create(getRoleRepresentation(key));
                        return true;
                    }));
            realmResource.clients().create(getClientRepresentation(new ClientRepresentation(), cytominePermissions));
        });
    }

    RoleRepresentation getRoleRepresentation(String role) {
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setId(role);
        roleRepresentation.setName(role);
        return roleRepresentation;
    }

    ClientRepresentation getClientRepresentation(ClientRepresentation clientRepresentation,
        CytominePermissions cytominePermissions) {
        clientRepresentation.setId("cytomine-permissions");
        clientRepresentation.setName("cytomine-permissions");
        clientRepresentation.setAuthorizationServicesEnabled(true);
        clientRepresentation.setServiceAccountsEnabled(true);
        clientRepresentation.setAuthorizationSettings(getResourceServerRepresentation(cytominePermissions));
        return clientRepresentation;
    }

    ResourceServerRepresentation getResourceServerRepresentation(CytominePermissions cytominePermissions) {
        ResourceServerRepresentation resourceServerRepresentation = new ResourceServerRepresentation();
        resourceServerRepresentation.setResources(List.of(getResourceRepresentation()));


        resourceServerRepresentation.setScopes(
            Stream.of(CytominePermission.values()).map(this::getScopeRepresentation).toList());
        resourceServerRepresentation.setPolicies(
            Stream.of(CytominePermission.values()).map(this::getPolicyRepresentation).toList());
        cytominePermissions.roles().forEach((roleName, permissions) -> {
        });
        return resourceServerRepresentation;
    }

    ScopeRepresentation getScopeRepresentation(CytominePermission scope) {
        ScopeRepresentation scopeRepresentation = new ScopeRepresentation();
        scopeRepresentation.setId(scope.toString());
        scopeRepresentation.setName(scope.toString());
        return scopeRepresentation;
    }

    @SneakyThrows
    PolicyRepresentation getPolicyRepresentation(CytominePermission policy) {
        PolicyRepresentation policyRepresentation = new PolicyRepresentation();
        policyRepresentation.setId(policy.toString());
        policyRepresentation.setName(policy.toString());
        policyRepresentation.setType("role");
        policyRepresentation.setScopes(Set.of(policy.toString()));
        policyRepresentation.setDecisionStrategy(UNANIMOUS);
        policyRepresentation.setLogic(Logic.POSITIVE);
        policyRepresentation.setConfig(Map.of("roles",
            jsonMapper.writeValueAsString(List.of(new SimpleRoleRepresentation(policy.toString(), false)))));
        return policyRepresentation;

    }

    ResourceRepresentation getResourceRepresentation() {
        ResourceRepresentation resourceRepresentation = new ResourceRepresentation();
        resourceRepresentation.setId("core");
        resourceRepresentation.setName("core");

        return resourceRepresentation;
    }


}
