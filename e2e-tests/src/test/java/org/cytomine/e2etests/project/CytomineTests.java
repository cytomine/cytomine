package org.cytomine.e2etests.project;

import lombok.SneakyThrows;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.cytomine.e2etests.ui.WebDriverUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Import({SeleniumDriver.class, CytomineSteps.class, WebDriverUtils.class})
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
    CytomineSteps cytomineSteps;

    @BeforeEach
    void setUp() {
        driver = driverProvider.driver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);  // Capture the screenshot as a file
        System.out.println("Screenshot captured at: " + screenshot.getAbsolutePath());

        // Define the destination path for saving the screenshot
        Path destination = Paths.get("./build/reports/" + screenshot.getName());
        Files.createDirectories(Path.of("./build/reports/"));
        // Move the screenshot to the desired location
        Files.move(screenshot.toPath(), destination, REPLACE_EXISTING);
        driver.close();
    }

    @Test
    void login() {
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
    }

    @Test
    void createProject() {
        String projectName = "selenium-" + UUID.randomUUID();
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.createProject(wait, cytomineUrl, projectName);
        cytomineSteps.deleteProject(wait, driver.getCurrentUrl());
    }

    @Test
    void deleteProject() {
        String projectName = "selenium-" + UUID.randomUUID();
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.createProject(wait, cytomineUrl, projectName);
        cytomineSteps.deleteProject(wait, driver.getCurrentUrl());
    }

}
