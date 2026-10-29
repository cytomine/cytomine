package org.cytomine.e2etests.lti;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wire-level LTI 1.3 deep-linking contract tests. The suite boots its own mock LMS platform
 * ({@link LtiPlatformSimulator}), seeds a dedicated Keycloak realm with the two platform
 * registrations ({@link LtiRealmSeeder}) and drives the complete browser flow through the
 * running IAM service ({@link LtiFlowClient}) for a Moodle-shaped and a Blackboard-shaped
 * platform, plus spec variants. It covers the two Blackboard fixes of issue #1032: the
 * top-level {@code data} claim round-trip and the {@code window} presentation property.
 *
 * <p>Requires a running IAM service (default {@code http://localhost:8100/iam}); the tests
 * skip gracefully when it is not reachable. Run it locally with:
 * {@code docker compose up -d iam-db iam && ./gradlew :e2e-tests:test --tests "org.cytomine.e2etests.lti.*"}</p>
 */
class LtiDeepLinkingContractTest {

    private static final String DL_NAMESPACE = "https://purl.imsglobal.org/spec/lti-dl/claim/";
    private static final String LTI_NAMESPACE = "https://purl.imsglobal.org/spec/lti/claim/";

    private static final String CLAIM_MESSAGE_TYPE = LTI_NAMESPACE + "message_type";
    private static final String CLAIM_VERSION = LTI_NAMESPACE + "version";
    private static final String CLAIM_DEPLOYMENT_ID = LTI_NAMESPACE + "deployment_id";
    private static final String CLAIM_ROLES = LTI_NAMESPACE + "roles";
    private static final String CLAIM_CUSTOM = LTI_NAMESPACE + "custom";
    private static final String LTI_ROLES_URI_PREFIX = "http://purl.imsglobal.org/vocab/lis/v2/membership#";
    private static final String CLAIM_DEEP_LINKING_SETTINGS = DL_NAMESPACE + "deep_linking_settings";
    private static final String CLAIM_CONTENT_ITEMS = DL_NAMESPACE + "content_items";
    private static final String CLAIM_DATA = DL_NAMESPACE + "data";

    private static final String ITEM_URL = "https://view.example.org/explore/items/1";
    private static final String ITEM_TITLE = "Fresh lookup";
    private static final String ITEM_TEXT = "Selected image of lesson";
    private static final String ITEM_CUSTOM = "{\"cytomine_project_id\":\"42\"}";
    private static final Map<String, Object> ITEM_CUSTOM_MAP = Map.of("cytomine_project_id", "42");

    private static final LtiPlatformSimulator PLATFORM = new LtiPlatformSimulator();
    private static final LtiRealmSeeder SEEDER = new LtiRealmSeeder();

    /**
     * Flow clients carry the browser cookies of one LMS session, so every test gets a fresh
     * "browser": an authenticated session from the previous scenario must not leak into the next one.
     */
    private static LtiFlowClient browser() {
        return new LtiFlowClient();
    }

    @BeforeAll
    static void startEnvironment() {
        Assumptions.assumeTrue(iamIsReachable(), "IAM service is not reachable - skipping LTI contract tests");
        PLATFORM.start();
        SEEDER.seed();
    }

    @AfterAll
    static void stopEnvironment() {
        PLATFORM.stop();
        SEEDER.dropRealm();
    }

    @Test
    void moodleDeepLinkingSelectionReturnsSpecConformantResponse() {
        LtiFixture fixture = LtiFixture.load("moodle");
        Map<String, Object> response = selectAndVerifyConsistentResponse(fixture, null);
        assertPlatformEcho(response, fixture);
        assertSelection(response, true);
        assertEquals("moodle-correlation-data-42", response.get(CLAIM_DATA));
        assertEquals("moodle-deployment-1", response.get(CLAIM_DEPLOYMENT_ID));
        assertEquals("teacher-1", response.get("sub"));
        assertProvisionedUser("anne.instructor@moodle.local", "USER", "join-token-moodle-project-42");
    }

    @Test
    void blackboardDeepLinkingSelectionEchoesDataAndOpensANewWindow() {
        LtiFixture fixture = LtiFixture.load("blackboard");
        Map<String, Object> response = selectAndVerifyConsistentResponse(fixture, null);
        assertPlatformEcho(response, fixture);
        assertSelection(response, true);
        assertEquals("bb-opaque-placement-data", response.get(CLAIM_DATA));
        assertEquals("bb-deployment-1", response.get(CLAIM_DEPLOYMENT_ID));
        assertEquals("bb-user-2", response.get("sub"));
        assertProvisionedUser("bob.teacher@blackboard.test", "USER", "join-token-bb-project-7");
    }

    @Test
    void platformThatRejectsWindowTargetsGetsNoWindowProperty() {
        UnaryOperator<Map<String, Object>> variant = claims -> {
            LtiFixture.setPresentationTargets(claims, List.of("iframe", "embed"));
            return claims;
        };
        Map<String, Object> response = selectAndVerifyConsistentResponse(LtiFixture.load("moodle"), variant);
        Map<String, Object> contentItem = soleContentItem(response);
        assertNull(contentItem.get("window"),
            "window property must be absent when the platform rejects window targets");
    }

    @Test
    void platformWithoutPresentationTargetsIsTreatedPermissively() {
        UnaryOperator<Map<String, Object>> variant = claims -> {
            LtiFixture.removeFromDeepLinkingSettings(claims, "accept_presentation_document_targets");
            return claims;
        };
        Map<String, Object> response = selectAndVerifyConsistentResponse(LtiFixture.load("moodle"), variant);
        Map<String, Object> contentItem = soleContentItem(response);
        assertEquals(Map.of("targetName", "_blank"), contentItem.get("window"),
            "an absent presentation-target list is treated as permissive");
    }

    @Test
    void requestWithoutDataYieldsNoDataClaim() {
        UnaryOperator<Map<String, Object>> variant = claims -> {
            LtiFixture.removeFromDeepLinkingSettings(claims, "data");
            return claims;
        };
        Map<String, Object> response = selectAndVerifyConsistentResponse(LtiFixture.load("moodle"), variant);
        assertNull(response.get(CLAIM_DATA), "no data claim must be returned when the request carried none");
    }

    @Test
    void selectionContextIsSingleUse() {
        LtiFixture fixture = LtiFixture.load("moodle");
        LtiFlowClient flowBrowser = browser();
        LtiFlowClient.PickerRedirect picker = flowBrowser.driveLaunch(
            PLATFORM, fixture.loginInitParameters(), fixture.copyRequestClaims(),
            LtiSettings.simulatorBase() + "/picker");
        flowBrowser.submitSelection(picker.ctxToken(), ITEM_URL, ITEM_TITLE, ITEM_TEXT, ITEM_CUSTOM);
        assertEquals(400, flowBrowser.replaySelection(picker.ctxToken()),
            "an already-consumed context token must be rejected");
    }

    @Test
    void learnerLaunchGrantsOnlyTheGuestCoreRole() {
        UnaryOperator<Map<String, Object>> variant = claims -> {
            claims.put("sub", "student-2");
            claims.put("email", "student2@moodle.local");
            claims.put("name", "Stella Student");
            claims.put(CLAIM_ROLES, List.of(LTI_ROLES_URI_PREFIX + "Student"));
            claims.put(CLAIM_CUSTOM, Map.of("cytomine_join_token", "join-token-student-9"));
            return claims;
        };
        Map<String, Object> response = selectAndVerifyConsistentResponse(LtiFixture.load("moodle"), variant);
        assertSelection(response, true);
        assertProvisionedUser("student2@moodle.local", "GUEST", "join-token-student-9");
    }

    @Test
    void repeatedLaunchesMergeFurtherMembershipTokensIntoTheUserAttribute() {
        UnaryOperator<Map<String, Object>> firstLaunch = claims -> {
            claims.put("sub", "student-3");
            claims.put("email", "student3@moodle.local");
            claims.put("name", "Sam Student");
            claims.put(CLAIM_ROLES, List.of(LTI_ROLES_URI_PREFIX + "Learner"));
            claims.put(CLAIM_CUSTOM, Map.of("cytomine_join_token", "join-token-course-a"));
            return claims;
        };
        selectAndVerifyConsistentResponse(LtiFixture.load("moodle"), firstLaunch);

        UnaryOperator<Map<String, Object>> secondLaunch = claims -> {
            claims.put("sub", "student-3");
            claims.put("email", "student3@moodle.local");
            claims.put(CLAIM_ROLES, List.of(LTI_ROLES_URI_PREFIX + "Learner"));
            claims.put(CLAIM_CUSTOM, Map.of("cytomine_join_token", "join-token-course-b"));
            return claims;
        };
        selectAndVerifyConsistentResponse(LtiFixture.load("moodle"), secondLaunch);

        List<String> tokens = SEEDER.attribute("student3@moodle.local", LtiRealmSeeder.JOIN_TOKEN_ATTRIBUTE);
        assertEquals(List.of("join-token-course-a", "join-token-course-b"), tokens,
            "membership tokens from repeated launches must merge, never replace");
        assertEquals(Set.of("GUEST"), SEEDER.coreRoleNames("student3@moodle.local"));
    }

    /**
     * Asserts the effects of the two seeded identity provider mappers on the Keycloak user that the
     * first-broker-login flow provisioned: the {@code LTI Core Roles} mapper grants exactly the mapped
     * client role on the Cytomine "core" client, and the {@code LTI Project Membership} mapper merged
     * the launch's {@code cytomine_join_token} custom parameter into the {@code lti_join_tokens} attribute.
     */
    private static void assertProvisionedUser(String username, String expectedCoreRole, String expectedToken) {
        assertTrue(SEEDER.userExists(username),
            "the broker first-login flow must provision the Keycloak user " + username);
        assertEquals(Set.of(expectedCoreRole), SEEDER.coreRoleNames(username),
            "the LTI Core Roles mapper must grant exactly the expected core client role");
        assertEquals(List.of(expectedToken), SEEDER.attribute(username, LtiRealmSeeder.JOIN_TOKEN_ATTRIBUTE),
            "the LTI Project Membership mapper must merge the launch join token");
    }

    private static Map<String, Object> selectAndVerifyConsistentResponse(LtiFixture fixture,
                                                                         UnaryOperator<Map<String, Object>> variant) {
        Map<String, Object> claims = fixture.copyRequestClaims();
        if (variant != null) {
            claims = variant.apply(claims);
        }
        LtiFlowClient flowBrowser = browser();
        LtiFlowClient.PickerRedirect picker = flowBrowser.driveLaunch(
            PLATFORM, fixture.loginInitParameters(), claims, LtiSettings.simulatorBase() + "/picker");
        LtiFlowClient.DeepLinkingResponseDelivery delivery =
            flowBrowser.submitSelection(picker.ctxToken(), ITEM_URL, ITEM_TITLE, ITEM_TEXT, ITEM_CUSTOM);
        assertEquals(PLATFORM.deepLinkReturnUrl(), delivery.formAction(),
            "the tool must return the selection to the deep_link_return_url declared by the platform");
        return verifiedResponseClaims(delivery.rawJwt());
    }

    /**
     * Validates what every {@code LtiDeepLinkingResponse} of this flow must satisfy, then hands
     * the decoded payload back to the calling test for scenario assertions.
     */
    private static Map<String, Object> verifiedResponseClaims(String rawJwt) {
        SignedJWT signedJwt;
        try {
            signedJwt = SignedJWT.parse(rawJwt);
            RSAKey realmKey = realmSigningKey();
            assertTrue(signedJwt.verify(new RSASSAVerifier(realmKey.toRSAPublicKey())),
                "response JWT must verify against the realm JWKS exposed for the tool");
            assertEquals(realmKey.getKeyID(), signedJwt.getHeader().getKeyID(),
                "response JWT must carry the matching realm signing key id");
            Map<String, Object> payload = signedJwt.getJWTClaimsSet().getClaims();
            assertEquals("LtiDeepLinkingResponse", payload.get(CLAIM_MESSAGE_TYPE));
            assertEquals("1.3.0", payload.get(CLAIM_VERSION));
            assertNotNull(payload.get("exp"));
            assertNotNull(payload.get("iat"));
            assertNotNull(payload.get("nonce"));
            assertFalse(payload.containsKey(CLAIM_DEEP_LINKING_SETTINGS),
                "the deep_linking_settings claim must never appear in the response (see issue #1032)");
            return payload;
        } catch (Exception e) {
            throw new IllegalStateException("Tool response JWT validation failed (" + e.getMessage() + ")", e);
        }
    }

    private static void assertPlatformEcho(Map<String, Object> response, LtiFixture fixture) {
        assertEquals(fixture.toolClientId(), response.get("iss"),
            "the response issuer must be the tool's client_id as registered with the platform");
        Object aud = response.get("aud");
        if (aud instanceof List<?> audiences) {
            assertTrue(audiences.contains(fixture.platformIssuer()),
                "the response audience must include the platform issuer");
        } else {
            assertEquals(fixture.platformIssuer(), aud,
                "the response audience must be the platform issuer");
        }
    }

    private static void assertSelection(Map<String, Object> response, boolean windowExpected) {
        Map<String, Object> contentItem = soleContentItem(response);
        assertEquals("ltiResourceLink", contentItem.get("type"));
        assertEquals(ITEM_URL, contentItem.get("url"));
        assertEquals(ITEM_TITLE, contentItem.get("title"));
        assertEquals(ITEM_CUSTOM_MAP, contentItem.get("custom"));
        if (windowExpected) {
            assertEquals(Map.of("targetName", "_blank"), contentItem.get("window"));
        } else {
            assertNull(contentItem.get("window"));
        }
    }

    private static Map<String, Object> soleContentItem(Map<String, Object> response) {
        Object contentItems = response.get(CLAIM_CONTENT_ITEMS);
        assertNotNull(contentItems, "content_items claim must be present");
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) contentItems;
        assertEquals(1, items.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> contentItem = (Map<String, Object>) items.get(0);
        return contentItem;
    }

    private static RSAKey realmSigningKey() {
        try {
            JWKSet jwks = JWKSet.load(new URL(LtiSettings.realmJwksUrl()), 2000, 2000, 64 * 1024);
            return (RSAKey) jwks.getKeys().stream()
                .filter(key -> KeyUse.SIGNATURE.equals(key.getKeyUse()))
                .filter(key -> key.getAlgorithm() == null || "RS256".equalsIgnoreCase(key.getAlgorithm().toString()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No RS256 signature key in the realm JWKS"));
        } catch (Exception e) {
            throw new IllegalStateException(
                "Could not load the realm JWKS from " + LtiSettings.realmJwksUrl(), e);
        }
    }

    private static boolean iamIsReachable() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
            HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(LtiSettings.IAM_BASE_URL + "/realms/master"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
