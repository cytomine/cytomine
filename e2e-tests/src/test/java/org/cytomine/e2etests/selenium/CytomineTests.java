package org.cytomine.e2etests.selenium;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Set;

import lombok.SneakyThrows;
import org.cytomine.e2etests.api.KeycloakClient;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.AnnotationTools;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.cytomine.e2etests.ui.WebDriverUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.util.UUID.randomUUID;
import static org.openqa.selenium.OutputType.FILE;

@Import({SeleniumDriver.class, AnnotationTools.class, CytomineSteps.class, WebDriverUtils.class, KeycloakClient.class})
@SpringBootTest
public class CytomineTests {
    @Autowired
    SeleniumDriver driverProvider;
    WebDriver driver;
    Wait<WebDriver> wait;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @Autowired
    AnnotationTools annotationTools;

    @Autowired
    CytomineSteps cytomineSteps;

    @Autowired
    KeycloakClient keycloakClient;

    @BeforeEach
    void setUp() {
        driver = driverProvider.driver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        saveScreenshot("closing-" + testInfo.getTestMethod()
            .map(Method::getName)
            .orElseGet(() -> "no-name-" + randomUUID()));
        driver.quit();
    }

    @SneakyThrows
    void saveScreenshot(String name) {
        Path destination = Paths.get("./build/reports/" + name + ".jpg");
        Files.createDirectories(Path.of("./build/reports/"));
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(FILE);
        Files.move(screenshot.toPath(), destination, REPLACE_EXISTING);
        Files.setPosixFilePermissions(destination, Set.of(OTHERS_READ, OWNER_READ, GROUP_READ));
    }

    @Test
    void checkProjectAfterPimsImport() {
        String projectName = "test-project";
        String imageName = "cat.png";
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);

        cytomineSteps.checkPimsImportProject(new WebDriverWait(driver, Duration.ofMinutes(5)),
            cytomineUrl, projectName, imageName);

        cytomineSteps.logout(wait, cytomineUrl);
    }

}
