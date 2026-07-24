package org.cytomine.e2etests.selenium;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;

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

@Import({AnnotationTools.class, CytomineSteps.class, KeycloakClient.class, SeleniumDriver.class, WebDriverUtils.class})
@SpringBootTest
public class UserTests {
    @Autowired
    CytomineSteps cytomineSteps;

    @Autowired
    SeleniumDriver driverProvider;

    @Autowired
    KeycloakClient keycloakClient;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    WebDriver driver;
    Wait<WebDriver> wait;

    @BeforeEach
    void setUp() {
        driver = driverProvider.driver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        Screenshots.save(
            driver,
            "closing-" + testInfo.getTestMethod().map(Method::getName).orElseGet(() -> "no-name-" + randomUUID())
        );
        driver.quit();
    }

    @Test
    void login() {
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
    }

    @Test
    void logout() {
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.logout(wait, cytomineUrl);
    }

    @Test
    void createNewUserAndLoginAsUser() {
        String username = "selenium-user-" + randomUUID().toString().substring(0, 8);
        String firstname = "Selenium";
        String lastname = "User-" + randomUUID().toString().substring(0, 8);
        String email = username + "@selenium.test";
        String password = "Selenium1!";

        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.createUser(wait, cytomineUrl, username, firstname, lastname, email, password);
        cytomineSteps.logout(wait, cytomineUrl);

        cytomineSteps.login(wait, cytomineUrl, username, password);
        cytomineSteps.logout(wait, cytomineUrl);

        keycloakClient.deleteUser(username);
    }

    @Test
    void editUser() {
        String username = "selenium-user-" + randomUUID().toString().substring(0, 8);
        String firstname = "Selenium";
        String lastname = "User-" + randomUUID().toString().substring(0, 8);
        String email = username + "@selenium.test";
        String password = "Selenium1!";

        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.createUser(wait, cytomineUrl, username, firstname, lastname, email, password);
        cytomineSteps.editUser(wait, cytomineUrl, username, "UpdatedFirst", "UpdatedLast");
        cytomineSteps.logout(wait, cytomineUrl);
        keycloakClient.deleteUser(username);
    }
}
