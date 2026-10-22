package org.cytomine.repository.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionsService {

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
    private static final int MAX_ATTEMPTS = 60;
    private static final long RETRY_DELAY_SECONDS = 10;

    private final Keycloak keycloak;

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

    @PostConstruct
    public void applyCytomineConfiguration() {
        RealmResource realmResource = ensureRealmExists();
        ClientResource coreClient = ensureCoreClientExists(realmResource);
        ensureClientRolesAreSet(coreClient);
        ensureAdminUserIsSet(realmResource, coreClient);
    }

    private RealmResource ensureRealmExists() {
        RealmsResource realmsResource = keycloak.realms();
        if (realmsResource.findAll().stream().noneMatch(r -> realm.equals(r.getRealm()))) {
            RealmRepresentation realmRepresentation = new RealmRepresentation();
            realmRepresentation.setRealm(realm);
            realmsResource.create(realmRepresentation);
            log.info("Created Keycloak realm '{}'", realm);
        }
        return keycloak.realm(realm);
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
