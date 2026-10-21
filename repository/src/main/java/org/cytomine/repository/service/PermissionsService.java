package org.cytomine.repository.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cytomine.repository.config.CytominePermissions;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.AuthorizationResource;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourcePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.config.security.CytominePermission;
import be.cytomine.common.config.security.CytomineUserRole;

import static org.keycloak.representations.idm.authorization.DecisionStrategy.AFFIRMATIVE;
import static org.keycloak.representations.idm.authorization.DecisionStrategy.UNANIMOUS;

@Component
@Slf4j
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

        cytominePermissions.roles().forEach(
            (key, value) -> wrapException(() -> existingClient.roles().get(key).toRepresentation()).map(ignored -> true)
                .orElseGet(() -> {
                    existingClient.roles().create(getRoleRepresentation(key));
                    return true;
                }));

        existingClient.authorization()
            .importSettings(getResourceServerRepresentation(cytominePermissions));

        cytominePermissions.roles().forEach(
            (role, scopes) -> upsertResourcePermission(role, scopes, existingClient));

        cytominePermissions.users().forEach(user -> assignRolesToExistingUser(user, existingClient));
    }

    void upsertResourcePermission(String role, Set<CytominePermission> scopes, ClientResource client) {
        AuthorizationResource authorization = client.authorization();

        ResourcePermissionRepresentation permission =
            wrapException(() -> authorization.permissions().resource().findByName("core-" + role))
                .orElseGet(ResourcePermissionRepresentation::new);

        permission.setName("core-" + role);
        permission.setDecisionStrategy(UNANIMOUS);
        permission.setLogic(Logic.POSITIVE);
        permission.addResource("core");
        scopes.stream().map(CytominePermission::toString).forEach(permission::addScope);
        permission.addPolicy("role-" + role);

        try {
            if (permission.getId() == null) {
                authorization.permissions().resource().create(permission);
                log.info("Created resource permission core-{}", role);
            } else {
                authorization.permissions().resource()
                    .findById(permission.getId()).update(permission);
                log.info("Updated resource permission core-{}", role);
            }
        } catch (Exception e) {
            log.error("Failed to upsert resource permission core-{}", role, e);
        }
    }

    void assignRolesToExistingUser(CytomineUserRole user, ClientResource client) {
        UsersResource users = keycloak.realm(realm).users();

        Optional<UserRepresentation> existingUser =
            wrapException(() -> users.searchByUsername(user.name(), true))
                .filter(found -> !found.isEmpty())
                .map(found -> found.get(0));

        if (existingUser.isEmpty()) {
            log.warn("User {} not found in realm {}, skipping roles assignment", user.name(), realm);
            return;
        }

        List<RoleRepresentation> userRoles = user.roles().stream()
            .map(role -> wrapException(() -> client.roles().get(role).toRepresentation()))
            .flatMap(Optional::stream)
            .toList();

        users.get(existingUser.get().getId())
            .roles()
            .clientLevel(client.toRepresentation().getId())
            .add(userRoles);
        log.info("Assigned client roles {} to user {}", user.roles(), user.name());
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
        Set<CytominePermission> scopes = Stream.of(CytominePermission.values())
            .collect(Collectors.toSet());

        resourceServerRepresentation.setDecisionStrategy(AFFIRMATIVE);
        resourceServerRepresentation.setScopes(
            scopes.stream().map(this::getScopeRepresentation).toList());
        resourceServerRepresentation.setResources(List.of(getResourceRepresentation(scopes)));
        resourceServerRepresentation.setPolicies(cytominePermissions.roles().keySet().stream()
            .map(this::getRolePolicy)
            .toList());
        return resourceServerRepresentation;
    }

    ScopeRepresentation getScopeRepresentation(CytominePermission scope) {
        ScopeRepresentation scopeRepresentation = new ScopeRepresentation();
        scopeRepresentation.setId(scope.toString());
        scopeRepresentation.setName(scope.toString());
        return scopeRepresentation;
    }

    @SneakyThrows
    PolicyRepresentation getRolePolicy(String role) {
        PolicyRepresentation policyRepresentation = new PolicyRepresentation();
        policyRepresentation.setId("role-" + role);
        policyRepresentation.setName("role-" + role);
        policyRepresentation.setType("role");
        policyRepresentation.setDecisionStrategy(UNANIMOUS);
        policyRepresentation.setLogic(Logic.POSITIVE);
        policyRepresentation.setConfig(
            Map.of("roles", jsonMapper.writeValueAsString(
                List.of(new SimpleRoleRepresentation("cytomine-permissions/" + role, false)))));
        return policyRepresentation;

    }

    ResourceRepresentation getResourceRepresentation(Set<CytominePermission> scopes) {
        ResourceRepresentation resourceRepresentation = new ResourceRepresentation();
        resourceRepresentation.setId("core");
        resourceRepresentation.setName("core");
        resourceRepresentation.setScopes(
            scopes.stream().map(this::getScopeRepresentation).collect(Collectors.toSet()));
        return resourceRepresentation;
    }

}
