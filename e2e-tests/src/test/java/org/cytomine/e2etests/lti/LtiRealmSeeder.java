package org.cytomine.e2etests.lti;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * Creates (and removes) the dedicated {@link LtiSettings#REALM} Keycloak realm with the tool client
 * and the LTI 1.3 identity providers matching the fixtures, once per test class run. Only the IAM
 * master admin API is used; the rest of the Cytomine stack is not touched.
 */
final class LtiRealmSeeder {

    static final String TOOL_CLIENT_ID = "lti-tool";

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
        createToolClient(LtiSettings.SIMULATOR_PORT);
        seedIdentityProvider(LtiFixture.load("moodle"));
        seedIdentityProvider(LtiFixture.load("blackboard"));
    }

    void dropRealm() {
        try {
            admin.realms().realm(LtiSettings.REALM).remove();
        } catch (ForbiddenException e) {
            throw new IllegalStateException(
                "IAM admin credentials seem to be wrong for " + LtiSettings.IAM_BASE_URL, e);
        } catch (NotFoundException freshEnvironment) {
            // realm does not exist yet - nothing to clean up
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
    }
}
