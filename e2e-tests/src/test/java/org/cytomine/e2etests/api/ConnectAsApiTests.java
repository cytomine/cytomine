package org.cytomine.e2etests.api;

import java.time.Duration;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.cytomine.e2etests.api.CoreApiClient.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@Import({CoreApiClient.class, KeycloakClient.class})
public class ConnectAsApiTests {

    private static final String PASSWORD = "Selenium1!";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String REALM_MANAGEMENT = "realm-management";
    private static final String IMPERSONATION = "impersonation";

    @Autowired
    CoreApiClient api;

    @Autowired
    KeycloakClient keycloakClient;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @Test
    void newAdminGetsImpersonationAndCanConnectAsAnotherUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String newAdminName = "selenium-connectas-admin-" + suffix;
        String targetName = "selenium-connectas-user-" + suffix;
        String adminToken = api.passwordToken(adminUsername, adminPassword);
        long newAdminId = 0;
        long targetUserId = 0;

        try {
            newAdminId = api.createUser(adminToken, newAdminName, PASSWORD, ROLE_ADMIN);
            targetUserId = api.createUser(adminToken, targetName, PASSWORD, ROLE_USER);

            assertTrue(
                waitFor(
                    () -> keycloakClient.hasClientRole(newAdminName, REALM_MANAGEMENT, IMPERSONATION),
                    Duration.ofSeconds(10)
                ),
                "New ADMIN should receive realm-management:impersonation"
            );

            String newAdminToken = api.passwordToken(newAdminName, PASSWORD);
            String targetIamId = keycloakClient.findUserId(targetName).orElseThrow();
            ApiResponse response = api.connectAs(newAdminToken, targetUserId);
            assertEquals(200, response.status(), () -> "connect-as failed: " + response.rawBody());
            assertEquals(targetUserId, response.body().path("userId").asLong());
            assertEquals(targetName, response.body().path("username").asText());
            assertEquals(newAdminName, response.body().path("actorUsername").asText());

            String actingToken = response.body().path("accessToken").asText(null);
            assertNotNull(actingToken, "connect-as response should contain an access token");
            JsonNode actingClaims = api.decodeJwt(actingToken);
            assertEquals(targetIamId, actingClaims.path("sub").asText());
            assertEquals(targetName, actingClaims.path("preferred_username").asText());

            ApiResponse selfResponse = api.connectAs(newAdminToken, newAdminId);
            assertEquals(
                403,
                selfResponse.status(),
                () -> "self connect-as should be forbidden: " + selfResponse.rawBody()
            );
        } finally {
            deleteQuietly(adminToken, targetUserId);
            deleteQuietly(adminToken, newAdminId);
            deleteIamUserQuietly(targetName);
            deleteIamUserQuietly(newAdminName);
        }
    }

    @Test
    void demotedAdminLosesImpersonationAndCannotConnectAs() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String newAdminName = "selenium-connectas-demoted-" + suffix;
        String targetName = "selenium-connectas-target-" + suffix;
        String adminToken = api.passwordToken(adminUsername, adminPassword);
        long newAdminId = 0;
        long targetUserId = 0;

        try {
            newAdminId = api.createUser(adminToken, newAdminName, PASSWORD, ROLE_ADMIN);
            targetUserId = api.createUser(adminToken, targetName, PASSWORD, ROLE_USER);
            assertTrue(
                waitFor(
                    () -> keycloakClient.hasClientRole(newAdminName, REALM_MANAGEMENT, IMPERSONATION),
                    Duration.ofSeconds(10)
                ),
                "New ADMIN should receive realm-management:impersonation"
            );

            api.updateUserRole(adminToken, newAdminId, newAdminName, ROLE_USER);

            assertTrue(
                waitFor(
                    () -> !keycloakClient.hasClientRole(newAdminName, REALM_MANAGEMENT, IMPERSONATION),
                    Duration.ofSeconds(10)
                ),
                "Demoted ADMIN should lose realm-management:impersonation"
            );

            String demotedToken = api.passwordToken(newAdminName, PASSWORD);
            ApiResponse response = api.connectAs(demotedToken, targetUserId);
            assertEquals(
                403,
                response.status(),
                () -> "demoted admin should no longer be allowed to connect as: " + response.rawBody()
            );
        } finally {
            deleteQuietly(adminToken, targetUserId);
            deleteQuietly(adminToken, newAdminId);
            deleteIamUserQuietly(targetName);
            deleteIamUserQuietly(newAdminName);
        }
    }

    private boolean waitFor(BooleanSupplier condition, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return condition.getAsBoolean();
    }

    private void deleteQuietly(String adminToken, long userId) {
        try {
            api.deleteUser(adminToken, userId);
        } catch (Exception e) {
            log.warn("Unable to delete test user {}: {}", userId, e.getMessage());
        }
    }

    private void deleteIamUserQuietly(String username) {
        try {
            keycloakClient.deleteUser(username);
        } catch (Exception e) {
            log.warn("Unable to delete IAM test user {}: {}", username, e.getMessage());
        }
    }
}
