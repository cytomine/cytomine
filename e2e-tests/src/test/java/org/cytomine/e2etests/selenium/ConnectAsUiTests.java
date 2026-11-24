package org.cytomine.e2etests.selenium;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import lombok.extern.slf4j.Slf4j;
import org.cytomine.e2etests.api.KeycloakClient;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.AnnotationTools;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.cytomine.e2etests.ui.WebDriverUtils;
import org.cytomine.e2etests.utils.Screenshots;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Import({AnnotationTools.class, CytomineSteps.class, KeycloakClient.class, SeleniumDriver.class, WebDriverUtils.class})
@SpringBootTest
public class ConnectAsUiTests {

    private static final String PASSWORD = "Selenium1!";
    private static final String REALM_MANAGEMENT = "realm-management";
    private static final String IMPERSONATION = "impersonation";

    @Autowired
    SeleniumDriver driverProvider;

    @Autowired
    CytomineSteps cytomineSteps;

    @Autowired
    KeycloakClient keycloakClient;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    WebDriver driver;
    Wait<WebDriver> wait;

    @BeforeEach
    void setUp() {
        driver = driverProvider.driver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        Screenshots.save(driver,
            "closing-" + testInfo.getTestMethod().map(Method::getName).orElseGet(() -> "no-name-" + randomUUID()));
        driver.quit();
    }

    @Test
    void newAdminCanConnectAsUserFromAdminPanel() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String newAdminName = "selenium-connectas-admin-" + suffix;
        String newAdminLastname = "ConnectAsAdmin" + suffix;
        String targetName = "selenium-connectas-user-" + suffix;
        String targetLastname = "ConnectAsUser" + suffix;

        try {
            cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
            cytomineSteps.createUser(
                wait,
                cytomineUrl,
                newAdminName,
                "Selenium",
                newAdminLastname,
                newAdminName + "@selenium.test",
                PASSWORD,
                "Admin"
            );
            cytomineSteps.createUser(
                wait,
                cytomineUrl,
                targetName,
                "Selenium",
                targetLastname,
                targetName + "@selenium.test",
                PASSWORD,
                "User"
            );

            assertTrue(
                waitFor(
                    () -> keycloakClient.hasClientRole(newAdminName, REALM_MANAGEMENT, IMPERSONATION),
                    Duration.ofSeconds(10)
                ),
                "New ADMIN created from the admin panel should receive realm-management:impersonation"
            );

            cytomineSteps.logout(wait, cytomineUrl);
            cytomineSteps.login(wait, cytomineUrl, newAdminName, PASSWORD);

            cytomineSteps.verifyConnectAsVisible(wait, cytomineUrl, newAdminName, newAdminLastname, false);
            cytomineSteps.verifyConnectAsVisible(wait, cytomineUrl, targetName, targetLastname, true);

            cytomineSteps.connectAs(wait, cytomineUrl, targetName, targetLastname);
            cytomineSteps.verifyActingAs(wait, targetName);
            cytomineSteps.verifyAdminMenuVisible(wait, false);

            cytomineSteps.exitActingAs(wait);
            cytomineSteps.verifyAdminMenuVisible(wait, true);
        } finally {
            cleanup(() -> cytomineSteps.exitActingAsIfNeeded(wait));
            cleanup(() -> cytomineSteps.logout(wait, cytomineUrl));
            cleanup(() -> cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword));
            cleanup(() -> cytomineSteps.deleteUser(wait, cytomineUrl, targetName, targetLastname));
            cleanup(() -> cytomineSteps.deleteUser(wait, cytomineUrl, newAdminName, newAdminLastname));
            cleanup(() -> keycloakClient.deleteUser(targetName));
            cleanup(() -> keycloakClient.deleteUser(newAdminName));
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

    private void cleanup(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.warn("Cleanup step failed: {}", e.getMessage(), e);
        }
    }
}
