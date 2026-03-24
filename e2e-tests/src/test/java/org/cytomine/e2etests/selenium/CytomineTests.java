package org.cytomine.e2etests.selenium;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import lombok.SneakyThrows;
import org.cytomine.e2etests.configuration.SeleniumDriver;
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
import static java.util.stream.Collectors.toSet;
import static org.openqa.selenium.OutputType.FILE;

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
    void login() {
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
    }

    @Test
    void createAndDeleteProject() {
        String projectName = "selenium-" + randomUUID();
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
        String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
        cytomineSteps.deleteProject(wait, projectURL);
        if (ontologyURL != null) {
            cytomineSteps.deleteOntology(wait, ontologyURL);
        }
    }

    @Test
    void listProjects() {
        Set<String> projectNames =
            Set.of(
                "selenium-" + randomUUID(),
                "selenium-" + randomUUID(),
                "selenium-" + randomUUID());
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        Set<String> projectUrls =
            projectNames.stream()
                .map(name -> cytomineSteps.createProject(wait, driver, cytomineUrl, name))
                .collect(toSet());
        cytomineSteps.listProjects(wait, cytomineUrl, projectNames);
        Set<String> ignored =
            projectUrls.stream()
                .map(projectURL -> {
                    String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
                    cytomineSteps.deleteProject(wait, projectURL);
                    if (ontologyURL != null) {
                        cytomineSteps.deleteOntology(wait, ontologyURL);
                    }
                    return projectURL;
                })
                .collect(toSet());
    }

    @Test
    void createAndDeleteOntology() {
        String ontologyName = "selenium-" + randomUUID();
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String ontologyURL = cytomineSteps.createOntology(wait, driver, cytomineUrl, ontologyName);
        cytomineSteps.deleteOntology(wait, ontologyURL);
    }

    @Test
    void addImageToStorageNoProject() {
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String imageName = cytomineSteps.addImage(wait, cytomineUrl, Optional.empty());
        cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
    }

    @Test
    void addImageToStorageWithProject() {
        String projectName = "selenium-" + randomUUID();
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
        String imageName = cytomineSteps.addImage(wait, cytomineUrl, Optional.of(projectName));
        cytomineSteps.deleteProject(wait, projectURL);
        cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
    }

    @Test
    void addTermToOntology() {
        String ontologyName = "selenium-ontology-" + randomUUID();
        String termName = "selenium-term-" + randomUUID();
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String ontologyURL = cytomineSteps.createOntology(wait, driver, cytomineUrl, ontologyName);
        cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
        cytomineSteps.deleteTermFromOntology(wait, ontologyURL, termName);
        cytomineSteps.deleteOntology(wait, ontologyURL);
    }

    @Test
    void addAnnotationWithTools() {

    }

    @Test
    void addAnnotationWithTerm() {
        String projectName = "selenium-" + randomUUID();
        String termName = "selenium-term-" + randomUUID();

        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
        String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
        cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
        String imageName = cytomineSteps.addImage(wait, cytomineUrl, Optional.of(projectName));
        cytomineSteps.openImageInViewer(wait, driver, projectURL);
        cytomineSteps.selectTermForAnnotation(wait, termName);
        cytomineSteps.drawRectangleAnnotation(wait, driver);
        cytomineSteps.verifyAnnotationCreated(wait);
        cytomineSteps.deleteProject(wait, projectURL);
        cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        cytomineSteps.deleteOntology(wait, ontologyURL);
    }

    @Test
    void addAnnotationWithSam() {
        String projectName = "selenium-" + randomUUID();
        String termName = "selenium-term-" + randomUUID();

        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
        String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
        cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
        String imageName = cytomineSteps.addImage(wait, cytomineUrl, Optional.of(projectName));
        cytomineSteps.openImageInViewer(wait, driver, projectURL);
        cytomineSteps.selectTermForAnnotation(wait, termName);

        cytomineSteps.drawRectangleAnnotationWithMagicWand(wait, driver);
        cytomineSteps.verifyAnnotationProcessedWithSam(wait);

        cytomineSteps.deleteProject(wait, projectURL);
        cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        cytomineSteps.deleteOntology(wait, ontologyURL);
    }

    @Test
    void uploadAndDeleteTask() {
        String zipName = "com.cytomine.dummy.identity.image-1.0.0.zip";
        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.uploadTask(wait, cytomineUrl, zipName);
        cytomineSteps.deleteTask(wait, cytomineUrl, "identity with image");
    }

    @Test
    void runTask() {
        String zipName = "com.cytomine.dummy.identity.geometry-1.0.0.zip";
        String projectName = "selenium-" + randomUUID();
        String taskName = "identity with geometry";
        String taskVersion = "1.0.0";

        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.uploadTask(wait, cytomineUrl, zipName);
        String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
        String imageName = cytomineSteps.addImage(wait, cytomineUrl, Optional.of(projectName));
        cytomineSteps.openImageInViewer(wait, driver, projectUrl);
        cytomineSteps.drawRectangleAnnotation(wait, driver);
        cytomineSteps.verifyAnnotationCreated(wait);

        cytomineSteps.selectTask(wait, taskName, taskVersion);
        cytomineSteps.selectAnnotationForGeometryInput(wait);
        cytomineSteps.runTask(wait, driver);

        cytomineSteps.deleteTask(wait, cytomineUrl, taskName);
        cytomineSteps.deleteProject(wait, projectUrl);
        cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
    }

    @Test
    void retrieveSimilarAnnotationWithCbir() {
        String projectName = "selenium-" + randomUUID();
        String termName = "selenium-term-" + randomUUID();
        int nbAnnotations = 3;

        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
        String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
        cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
        String imageName = cytomineSteps.addImage(wait, cytomineUrl, Optional.of(projectName));
        cytomineSteps.openImageInViewer(wait, driver, projectURL);
        cytomineSteps.selectTermForAnnotation(wait, termName);

        for (int i = 0; i < nbAnnotations; i++) {
            cytomineSteps.drawRandomRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);
        }

        cytomineSteps.createAnnotationAndSearchAnnotations(wait, driver, nbAnnotations);

        cytomineSteps.deleteProject(wait, projectURL);
        cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        cytomineSteps.deleteOntology(wait, ontologyURL);
    }
}
