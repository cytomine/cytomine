package org.cytomine.repository.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourcePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import be.cytomine.common.config.security.CytominePermission;
import be.cytomine.common.config.security.CytomineUserRole;

import static org.keycloak.representations.idm.authorization.DecisionStrategy.AFFIRMATIVE;
import static org.keycloak.representations.idm.authorization.DecisionStrategy.UNANIMOUS;

@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionsService {

    public static final String CLIENT_NAME = "cytomine-permissions";
    public static final String RESOURCE_NAME = "core";
    private static final String NAME = "Cytomine Core";
    private static final String DESCRIPTION = "This is the default client for cytomine.";
    private static final List<String> ROLES = List.of("USER", "GUEST", "ADMIN");
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String IS_DEVELOPER_ATTRIBUTE = "isDeveloper";
    private static final String DEFAULT_IS_DEVELOPER_VALUE = "0";
    private static final List<String> DEFAULT_CLIENT_SCOPES =
        List.of("web-origins", "acr", "roles", "profile", "basic", "email");
    private static final List<String> OPTIONAL_CLIENT_SCOPES =
        List.of("address", "phone", "offline_access", "microprofile-jwt");
    private static final List<String> WEB_ORIGINS = List.of("*");
    private static final String REALM_MANAGEMENT_CLIENT = "realm-management";
    private static final List<String> REALM_MANAGEMENT_ROLES =
        List.of("view-users", "manage-users", "query-users");
    private static final List<String> SUPPORTED_LOCALES = List.of("en", "fr", "es", "nl");
    private static final String DEFAULT_LOCALE = "en";
    private static final String PROFILE_CLIENT_SCOPE = "profile";
    private static final String PROJECTS_MAPPER = "projects";
    private static final String USER_PROFILE_SCHEMA = "/keycloak-profile.json";
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private final Keycloak keycloak;

    private final CytominePermissions cytominePermissions;

    private final ObjectMapper jsonMapper;

    @Value("${keycloak-client.target.realm}")
    String realm;

    @Value("${keycloak-client.target.client-id}")
    String clientId;

    @Value("${keycloak-client.target.client-url:http://127.0.0.1}")
    String clientUrl;

    @Value("${keycloak-client.target.admin-user.username:admin}")
    String adminUsername;

    @Value("${keycloak-client.target.admin-user.password:password}")
    String adminPassword;

    @Value("${keycloak-client.target.admin-user.first-name:Admin}")
    String adminFirstName;

    @Value("${keycloak-client.target.admin-user.last-name:Admin}")
    String adminLastName;

    @Value("${keycloak-client.target.admin-user.email:admin@cytomine.org}")
    String adminEmail;

    @Value("${keycloak-client.configure-keycloak}")
    boolean configureKeycloak;

    @PostConstruct
    public void applyCytomineConfiguration() {
        if (configureKeycloak) {
            RealmResource realmResource = ensureRealmExists();
            ClientResource coreClient = ensureCoreClientExists(realmResource);
            ensureClientRolesAreSet(coreClient);
            ensureAdminUserIsSet(realmResource, coreClient);
            ensureRealmIsInternationalized(realmResource);
            ensureAdminHasRealmManagementRoles(realmResource);
            applyUserProfileSchema(realmResource);
            ensureProjectsMapperIsSet(realmResource);
            writePermissionsToKeycloak();
        }
    }

    private RealmResource ensureRealmExists() {
        RealmResource realmResource = keycloak.realms().findAll().stream()
            .filter(r -> realm.equals(r.getRealm()))
            .findFirst()
            .map(r -> keycloak.realm(realm))
            .orElseGet(() -> {
                RealmRepresentation realmRepresentation = new RealmRepresentation();
                realmRepresentation.setRealm(realm);
                realmRepresentation.setEnabled(true);
                keycloak.realms().create(realmRepresentation);
                log.info("Created Keycloak realm '{}'", realm);
                return keycloak.realm(realm);
            });
        RealmRepresentation representation = realmResource.toRepresentation();
        if (!Boolean.TRUE.equals(representation.isEnabled())) {
            representation.setEnabled(true);
            realmResource.update(representation);
            log.info("Enabled Keycloak realm '{}'", realm);
        }
        return realmResource;
    }

    private ClientResource ensureCoreClientExists(RealmResource realmResource) {
        ClientsResource clients = realmResource.clients();
        if (clients.findByClientId(clientId).isEmpty()) {
            clients.create(getNewClientRepresentation());
            log.info("Created Keycloak client '{}'", clientId);
        }
        ClientResource clientResource = clients.findByClientId(clientId).stream().findFirst()
            .map(representation -> clients.get(representation.getId()))
            .orElseThrow(() -> new IllegalStateException("Failed to retrieve Keycloak client " + clientId));
        clientResource.update(getNewClientRepresentation());
        return clientResource;
    }

    private ClientRepresentation getNewClientRepresentation() {
        ClientRepresentation representation = new ClientRepresentation();
        representation.setClientId(clientId);
        representation.setName(NAME);
        representation.setDescription(DESCRIPTION);
        representation.setRootUrl(clientUrl);
        representation.setAdminUrl(clientUrl);
        representation.setBaseUrl(clientUrl);
        representation.setSurrogateAuthRequired(false);
        representation.setEnabled(true);
        representation.setAlwaysDisplayInConsole(false);
        representation.setClientAuthenticatorType("client-secret");
        representation.setRedirectUris(List.of(clientUrl + "/*"));
        representation.setWebOrigins(WEB_ORIGINS);
        representation.setNotBefore(0);
        representation.setBearerOnly(false);
        representation.setConsentRequired(false);
        representation.setStandardFlowEnabled(true);
        representation.setImplicitFlowEnabled(false);
        representation.setDirectAccessGrantsEnabled(true);
        representation.setServiceAccountsEnabled(false);
        representation.setPublicClient(true);
        representation.setFrontchannelLogout(true);
        representation.setProtocol("openid-connect");
        representation.setAttributes(clientAttributes());
        representation.setAuthenticationFlowBindingOverrides(Map.of());
        representation.setFullScopeAllowed(true);
        representation.setNodeReRegistrationTimeout(-1);
        representation.setDefaultClientScopes(DEFAULT_CLIENT_SCOPES);
        representation.setOptionalClientScopes(OPTIONAL_CLIENT_SCOPES);
        return representation;
    }

    private Map<String, String> clientAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("realm_client", "false");
        attributes.put("oidc.ciba.grant.enabled", "false");
        attributes.put("backchannel.logout.session.required", "true");
        attributes.put("login_theme", "keycloak");
        attributes.put("post.logout.redirect.uris", clientUrl + "/*");
        attributes.put("display.on.consent.screen", "false");
        attributes.put("oauth2.device.authorization.grant.enabled", "false");
        attributes.put("backchannel.logout.revoke.offline.tokens", "false");
        return attributes;
    }

    private void ensureClientRolesAreSet(ClientResource coreClient) {
        Set<String> existing = coreClient.roles().list().stream()
            .map(RoleRepresentation::getName)
            .collect(Collectors.toSet());
        List<String> missing = ROLES.stream().filter(role -> !existing.contains(role)).toList();
        RolesResource clientRoles = coreClient.roles();
        for (String role : missing) {
            RoleRepresentation representation = new RoleRepresentation();
            representation.setName(role);
            representation.setClientRole(true);
            clientRoles.create(representation);
        }
        if (!missing.isEmpty()) {
            log.info("Created missing Keycloak client roles {} on client '{}'", missing, clientId);
        }
    }

    private void ensureAdminUserIsSet(RealmResource realmResource, ClientResource coreClient) {
        UsersResource users = realmResource.users();
        UserRepresentation user = users.search(adminUsername, Boolean.TRUE).stream().findFirst().orElse(null);
        if (user == null) {
            users.create(newAdminUserRepresentation(new UserRepresentation()));
            log.info("Created Keycloak admin user '{}'", adminUsername);
            user = users.search(adminUsername, Boolean.TRUE).stream().findFirst()
                .orElseThrow(
                    () -> new IllegalStateException("Failed to retrieve Keycloak admin user " + adminUsername));
        }

        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setEmail(adminEmail);
        user.setFirstName(adminFirstName);
        user.setLastName(adminLastName);
        user.setAttributes(Map.of(IS_DEVELOPER_ATTRIBUTE, List.of(DEFAULT_IS_DEVELOPER_VALUE)));
        UserResource userResource = users.get(user.getId());
        userResource.update(user);
        userResource.resetPassword(passwordCredential());
        log.info("Updated Keycloak admin user '{}'", adminUsername);

        String coreClientId = coreClient.toRepresentation().getId();
        List<RoleRepresentation> assignedRoles =
            userResource.roles().clientLevel(coreClientId).listAll();
        if (assignedRoles.stream().noneMatch(role -> ADMIN_ROLE.equals(role.getName()))) {
            RoleRepresentation adminRole = coreClient.roles().list().stream()
                .filter(role -> ADMIN_ROLE.equals(role.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Keycloak client role " + ADMIN_ROLE));
            userResource.roles().clientLevel(coreClientId).add(List.of(adminRole));
            log.info("Assigned Keycloak client role '{}' to user '{}'", ADMIN_ROLE, adminUsername);
        }
    }

    private void ensureRealmIsInternationalized(RealmResource realmResource) {
        RealmRepresentation representation = realmResource.toRepresentation();
        if (Boolean.TRUE.equals(representation.isInternationalizationEnabled())
            && SUPPORTED_LOCALES.containsAll(representation.getSupportedLocales())
            && DEFAULT_LOCALE.equals(representation.getDefaultLocale())) {
            return;
        }
        representation.setInternationalizationEnabled(true);
        representation.setSupportedLocales(Set.copyOf(SUPPORTED_LOCALES));
        representation.setDefaultLocale(DEFAULT_LOCALE);
        realmResource.update(representation);
        log.info("Enabled internationalization on Keycloak realm '{}'", realm);
    }

    private void ensureAdminHasRealmManagementRoles(RealmResource realmResource) {
        UserRepresentation user = realmResource.users().search(adminUsername, Boolean.TRUE).stream()
            .findFirst().orElse(null);
        if (user == null) {
            throw new IllegalStateException("Missing Keycloak admin user " + adminUsername);
        }
        ClientRepresentation realmManagement = realmResource.clients().findByClientId(REALM_MANAGEMENT_CLIENT).stream()
            .findFirst().orElse(null);
        if (realmManagement == null) {
            return;
        }
        UserResource userResource = realmResource.users().get(user.getId());
        Set<String> assigned = userResource.roles().clientLevel(realmManagement.getId()).listAll().stream()
            .map(RoleRepresentation::getName)
            .collect(Collectors.toSet());
        List<RoleRepresentation> missing = realmResource.clients().get(realmManagement.getId()).roles().list().stream()
            .filter(role -> REALM_MANAGEMENT_ROLES.contains(role.getName()))
            .filter(role -> !assigned.contains(role.getName()))
            .toList();
        if (missing.isEmpty()) {
            return;
        }
        userResource.roles().clientLevel(realmManagement.getId()).add(missing);
        log.info("Assigned realm-management roles {} to user '{}'",
            missing.stream().map(RoleRepresentation::getName).toList(), adminUsername);
    }

    private void applyUserProfileSchema(RealmResource realmResource) {
        try (InputStream stream = getClass().getResourceAsStream(USER_PROFILE_SCHEMA)) {
            if (stream == null) {
                throw new IllegalStateException("Missing user profile schema resource " + USER_PROFILE_SCHEMA);
            }
            UPConfig schema = JSON_MAPPER.readValue(stream, UPConfig.class);
            UPConfig config = realmResource.users().userProfile().getConfiguration();
            schema.getGroups().stream()
                .filter(group -> config.getGroups().stream()
                    .noneMatch(existing -> group.getName().equals(existing.getName())))
                .forEach(config::addGroup);
            schema.getAttributes().forEach(config::addOrReplaceAttribute);
            config.setUnmanagedAttributePolicy(schema.getUnmanagedAttributePolicy());
            realmResource.users().userProfile().update(config);
            log.info("Applied user profile attributes {} on Keycloak realm '{}'",
                schema.getAttributes().stream().map(UPAttribute::getName).toList(), realm);
        } catch (IOException | JacksonException e) {
            throw new IllegalStateException("Failed to parse user profile schema " + USER_PROFILE_SCHEMA, e);
        }
    }

    private void ensureProjectsMapperIsSet(RealmResource realmResource) {
        ClientScopeRepresentation profileScope = realmResource.clientScopes().findAll().stream()
            .filter(scope -> PROFILE_CLIENT_SCOPE.equals(scope.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Missing Keycloak client scope " + PROFILE_CLIENT_SCOPE));
        if (realmResource.clientScopes().get(profileScope.getId()).getProtocolMappers().getMappers().stream()
            .noneMatch(mapper -> PROJECTS_MAPPER.equals(mapper.getName()))) {
            ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
            mapper.setName(PROJECTS_MAPPER);
            mapper.setProtocol("openid-connect");
            mapper.setProtocolMapper("oidc-usermodel-attribute-mapper");
            Map<String, String> config = new HashMap<>();
            config.put("user.attribute", PROJECTS_MAPPER);
            config.put("claim.name", PROJECTS_MAPPER);
            config.put("id.token.claim", "true");
            config.put("access.token.claim", "true");
            config.put("userinfo.token.claim", "true");
            config.put("jsonType.label", "String");
            config.put("multivalued", "true");
            mapper.setConfig(config);
            realmResource.clientScopes().get(profileScope.getId()).getProtocolMappers().createMapper(mapper);
            log.info("Added 'projects' protocol mapper on realm '{}'", realm);
        }
    }


    public void writePermissionsToKeycloak() {
        RealmResource realmResource = keycloak.realm(realm);

        ClientsResource clients = realmResource.clients();

        ClientResource existingClient =
            clients.findByClientId(CLIENT_NAME).stream().findFirst().map(ignored -> clients.get(CLIENT_NAME))
                .orElseGet(() -> {
                    realmResource.clients().create(getBaseClientRepresentation(new ClientRepresentation()));
                    return realmResource.clients().get(CLIENT_NAME);
                });

        cytominePermissions.roles().forEach(
            (key, value) -> wrapException(() -> existingClient.roles().get(key).toRepresentation()).map(ignored -> true)
                .orElseGet(() -> {
                    existingClient.roles().create(getRoleRepresentation(key));
                    return true;
                }));

        existingClient.authorization().importSettings(getResourceServerRepresentation(cytominePermissions));

        cytominePermissions.roles().forEach((role, scopes) -> upsertResourcePermission(role, scopes, existingClient));

        cytominePermissions.users().forEach(user -> assignRolesToExistingUser(user, existingClient));
    }

    void upsertResourcePermission(String role, Set<CytominePermission> scopes, ClientResource client) {
        AuthorizationResource authorization = client.authorization();

        ResourcePermissionRepresentation permission =
            wrapException(() -> authorization.permissions().resource().findByName("core-" + role)).orElseGet(
                ResourcePermissionRepresentation::new);

        permission.setName("core-" + role);
        permission.setDecisionStrategy(UNANIMOUS);
        permission.setLogic(Logic.POSITIVE);
        permission.addResource(RESOURCE_NAME);
        scopes.stream().map(CytominePermission::toString).forEach(permission::addScope);
        permission.addPolicy("role-" + role);

        try {
            if (permission.getId() == null) {
                authorization.permissions().resource().create(permission);
                log.info("Created resource permission core-{}", role);
            } else {
                authorization.permissions().resource().findById(permission.getId()).update(permission);
                log.info("Updated resource permission core-{}", role);
            }
        } catch (Exception e) {
            log.error("Failed to upsert resource permission core-{}", role, e);
        }
    }

    void assignRolesToExistingUser(CytomineUserRole targetedUser, ClientResource client) {
        UsersResource users = keycloak.realm(realm).users();
        wrapException(() -> keycloak.realm(realm).users().searchByUsername(targetedUser.name(), true))
            .flatMap(usersResult -> usersResult.stream().findFirst())
            .ifPresent(u -> {
                List<RoleRepresentation> userRoles = targetedUser.roles().stream()
                    .map(role -> wrapException(() -> client.roles().get(role).toRepresentation()))
                    .flatMap(Optional::stream).toList();
                users.get(u.getId()).roles().clientLevel(client.toRepresentation().getId()).add(userRoles);
                log.info("Assigned client roles {} to user {}", targetedUser.roles(), targetedUser.name());
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

    RoleRepresentation getRoleRepresentation(String role) {
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setId(role);
        roleRepresentation.setName(role);
        roleRepresentation.setClientRole(true);
        return roleRepresentation;
    }

    ClientRepresentation getBaseClientRepresentation(ClientRepresentation clientRepresentation) {
        clientRepresentation.setId(CLIENT_NAME);
        clientRepresentation.setName(CLIENT_NAME);
        clientRepresentation.setAuthorizationServicesEnabled(true);
        clientRepresentation.setServiceAccountsEnabled(true);
        return clientRepresentation;
    }

    ResourceServerRepresentation getResourceServerRepresentation(CytominePermissions cytominePermissions) {
        ResourceServerRepresentation resourceServerRepresentation = new ResourceServerRepresentation();
        Set<CytominePermission> scopes = Stream.of(CytominePermission.values()).collect(Collectors.toSet());

        resourceServerRepresentation.setDecisionStrategy(AFFIRMATIVE);
        resourceServerRepresentation.setScopes(scopes.stream().map(this::getScopeRepresentation).toList());
        resourceServerRepresentation.setResources(List.of(getResourceRepresentation(scopes)));
        resourceServerRepresentation.setPolicies(
            cytominePermissions.roles().keySet().stream().map(this::getRolePolicy).toList());
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
        policyRepresentation.setConfig(Map.of("roles",
            jsonMapper.writeValueAsString(List.of(new SimpleRoleRepresentation(CLIENT_NAME + "/" + role, false)))));
        return policyRepresentation;

    }

    ResourceRepresentation getResourceRepresentation(Set<CytominePermission> scopes) {
        ResourceRepresentation resourceRepresentation = new ResourceRepresentation();
        resourceRepresentation.setId(RESOURCE_NAME);
        resourceRepresentation.setName(RESOURCE_NAME);
        resourceRepresentation.setScopes(scopes.stream().map(this::getScopeRepresentation).collect(Collectors.toSet()));
        return resourceRepresentation;
    }

    private UserRepresentation newAdminUserRepresentation(UserRepresentation representation) {
        representation.setUsername(adminUsername);
        representation.setEnabled(true);
        representation.setEmailVerified(true);
        representation.setEmail(adminEmail);
        representation.setFirstName(adminFirstName);
        representation.setLastName(adminLastName);
        representation.setAttributes(Map.of(IS_DEVELOPER_ATTRIBUTE, List.of(DEFAULT_IS_DEVELOPER_VALUE)));
        return representation;
    }

    private CredentialRepresentation passwordCredential() {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(adminPassword);
        credential.setTemporary(false);
        return credential;
    }
}
