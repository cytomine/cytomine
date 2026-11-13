package org.cytomine.e2etests.lti;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserProfileResource;
import org.keycloak.representations.idm.ClientMappingsRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.userprofile.config.UPConfig;

/**
 * Creates (and removes) the dedicated {@link LtiSettings#REALM} Keycloak realm with the tool client,
 * the Cytomine-managed "core" client with its ADMIN/USER/GUEST client roles, and one LTI 1.3 identity
 * provider per platform fixture - each carrying the {@code LTI Core Roles} mapper and the
 * {@code LTI Project Membership} mapper exactly as documented in the admin guide.
 *
 * <p>Only the IAM master admin API is used; the rest of the Cytomine stack is not touched.</p>
 */
final class LtiRealmSeeder {

    static final String TOOL_CLIENT_ID = "lti-tool";
    static final String CORE_CLIENT_ID = "core";
    static final List<String> MANAGED_ROLES = List.of("ADMIN", "USER", "GUEST");
    static final String JOIN_TOKEN_ATTRIBUTE = "lti_join_tokens";

    private final Keycloak admin;

    LtiRealmSeeder() {
        this.admin = KeycloakBuilder.builder()
            .serverUrl(LtiSettings.IAM_BASE_URL)
            .realm("master")
            .clientId("admin-cli")
            .username(LtiSettings.IAM_ADMIN)
            .password(LtiSettings.IAM_ADMIN_PASSWORD)
            .grantType(OAuth2Constants.PASSWORD)
            .build();
    }

    void seed() {
        dropRealm();
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(LtiSettings.REALM);
        realm.setEnabled(true);
        realm.setSslRequired("NONE");
        admin.realms().create(realm);
        allowAdminManagedAttributes();
        createToolClient(LtiSettings.SIMULATOR_PORT);
        createCoreClientWithManagedRoles();
        seedIdentityProvider(LtiFixture.load("moodle"));
        seedIdentityProvider(LtiFixture.load("blackboard"));
    }

    /**
     * Keycloak 26 always enables the user profile and defaults {@code unmanagedAttributePolicy}
     * to {@code DISABLED} for new realms, which silently discards every write of an attribute
     * that is not declared in the profile and hides it from admin REST reads (verified directly
     * on the {@code user_attribute} table). The {@code lti_join_tokens} attribute written by the
     * {@code LTI Project Membership} mapper must persist exactly as written and be observable
     * by this seeder's introspection, so the policy is set to {@code ADMIN_EDIT}: admins (the
     * broker login flow) may write undeclared attributes.
     */
    private void allowAdminManagedAttributes() {
        UserProfileResource profile =
            admin.realms().realm(LtiSettings.REALM).users().userProfile();
        UPConfig config = profile.getConfiguration();
        config.setUnmanagedAttributePolicy(UPConfig.UnmanagedAttributePolicy.ADMIN_EDIT);
        profile.update(config);
    }

    void dropRealm() {
        try {
            admin.realms().realm(LtiSettings.REALM).remove();
        } catch (ForbiddenException e) {
            throw new IllegalStateException(
                "IAM admin credentials seem to be wrong for " + LtiSettings.IAM_BASE_URL, e);
        } catch (NotFoundException freshEnvironment) {
            // realm does not exist yet - nothing to clean up
        } catch (ProcessingException unreachable) {
            // JUnit runs this cleanup even when the IAM reachability gate already aborted the
            // tests (e.g. the helm kind pod has no IAM on localhost:8100); the transport-level
            // failure means no realm was ever seeded - nothing to clean up.
        }
    }

    private void createToolClient(int simulatorPort) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(TOOL_CLIENT_ID);
        client.setPublicClient(true);
        client.setStandardFlowEnabled(true);
        client.setRedirectUris(List.of("http://localhost:" + simulatorPort + "/*"));
        client.setWebOrigins(List.of("+"));
        admin.realms().realm(LtiSettings.REALM).clients().create(client);
    }

    /**
     * The Cytomine "core" client owns the ADMIN/USER/GUEST client roles the LTI Core Roles mapper
     * assigns. Without this client (with its roles) the mapper skips role assignment with a warning.
     */
    private void createCoreClientWithManagedRoles() {
        ClientRepresentation core = new ClientRepresentation();
        core.setClientId(CORE_CLIENT_ID);
        core.setEnabled(true);
        admin.realms().realm(LtiSettings.REALM).clients().create(core);
        for (String roleName : MANAGED_ROLES) {
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            admin.realms().realm(LtiSettings.REALM).clients().get(coreClientId()).roles().create(role);
        }
    }

    private String coreClientId() {
        return admin.realms().realm(LtiSettings.REALM).clients().findByClientId(CORE_CLIENT_ID).get(0).getId();
    }

    private void seedIdentityProvider(LtiFixture fixture) {
        IdentityProviderRepresentation identityProvider = new IdentityProviderRepresentation();
        identityProvider.setAlias(fixture.alias());
        identityProvider.setProviderId("lti-1p3");
        identityProvider.setEnabled(true);
        identityProvider.setTrustEmail(true);
        identityProvider.setConfig(Map.ofEntries(
            Map.entry("platformIssuer", fixture.platformIssuer()),
            Map.entry("toolClientId", fixture.toolClientId()),
            Map.entry("keycloakClientId", TOOL_CLIENT_ID),
            Map.entry("allowedDeploymentIds", fixture.loginInitParameter("lti_deployment_id")),
            Map.entry("platformAuthorizationEndpoint", LtiSettings.simulatorBase() + "/auth"),
            Map.entry("platformJwksUrl", LtiSettings.simulatorBase() + "/jwks"),
            Map.entry("contentSelectionUrl", LtiSettings.simulatorBase() + "/picker")));
        admin.realms().realm(LtiSettings.REALM).identityProviders().create(identityProvider);
        addCoreRolesMapper(fixture.alias());
        addProjectMembershipMapper(fixture.alias());
    }

    private void addCoreRolesMapper(String alias) {
        IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
        mapper.setName("lti-core-roles");
        mapper.setIdentityProviderAlias(alias);
        mapper.setIdentityProviderMapper("lti-core-role-mapper");
        mapper.setConfig(Map.ofEntries(
            Map.entry("adminRoleUris", ""),
            Map.entry("userRoleUris", "Instructor,TeachingAssistant"),
            Map.entry("guestRoleUris", "Student,Learner")));
        admin.realms().realm(LtiSettings.REALM).identityProviders().get(alias).addMapper(mapper);
    }

    private void addProjectMembershipMapper(String alias) {
        IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
        mapper.setName("lti-project-membership");
        mapper.setIdentityProviderAlias(alias);
        mapper.setIdentityProviderMapper("lti-project-membership-mapper");
        mapper.setConfig(Map.ofEntries(
            Map.entry("customTokenKey", "cytomine_join_token"),
            Map.entry("targetAttribute", JOIN_TOKEN_ATTRIBUTE)));
        admin.realms().realm(LtiSettings.REALM).identityProviders().get(alias).addMapper(mapper);
    }

    boolean userExists(String username) {
        return provisionedUser(username) != null;
    }

    /** Names of the client roles the provisioned user holds on the Cytomine "core" client. */
    Set<String> coreRoleNames(String username) {
        UserRepresentation user = provisionedUser(username);
        if (user == null) {
            return Set.of();
        }
        MappingsRepresentation mappings =
            admin.realms().realm(LtiSettings.REALM).users().get(user.getId()).roles().getAll();
        if (mappings.getClientMappings() == null || mappings.getClientMappings().get(CORE_CLIENT_ID) == null) {
            return Set.of();
        }
        ClientMappingsRepresentation coreMappings = mappings.getClientMappings().get(CORE_CLIENT_ID);
        return coreMappings.getMappings().stream()
            .map(RoleRepresentation::getName)
            .collect(Collectors.toSet());
    }

    /** Values of a (possibly multivalued) user attribute, empty when the user or attribute is absent. */
    List<String> attribute(String username, String attributeName) {
        UserRepresentation user = provisionedUser(username);
        if (user == null || user.getAttributes() == null) {
            return List.of();
        }
        List<String> values = user.getAttributes().get(attributeName);
        return values == null ? List.of() : values;
    }

    private UserRepresentation provisionedUser(String username) {
        return admin.realms().realm(LtiSettings.REALM).users().search(username, 0, 5).stream()
            .filter(user -> username.equals(user.getUsername()))
            .findFirst()
            .orElse(null);
    }
}
