package org.cytomine.e2etests.selenium;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.SneakyThrows;
import org.cytomine.e2etests.api.KeycloakClient;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.AnnotationTools;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.cytomine.e2etests.ui.WebDriverUtils;
import org.cytomine.e2etests.utils.MultiUsersRunner;
import org.cytomine.e2etests.utils.ReportType;
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

import static be.cytomine.common.repository.model.Role.ROLE_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_USER;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;

@Import({AnnotationTools.class, CytomineSteps.class, KeycloakClient.class, MultiUsersRunner.class, SeleniumDriver.class,
    WebDriverUtils.class})
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

    @Autowired
    MultiUsersRunner multiUsers;

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

    @SneakyThrows
    private void sleep(long millis) {
        Thread.sleep(millis);
    }

    private void cleanup(Runnable... actions) {
        for (Runnable action : actions) {
            try {
                action.run();
            } catch (Exception e) {
                // best-effort cleanup; the original test failure must not be masked
            }
        }
    }

    @Test
    void manageTagsInAdminPanel() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String firstTagName = "selenium-tag-a-" + randomUUID();
            String secondTagName = "selenium-tag-b-" + randomUUID();
            String renamedTagName = "selenium-tag-renamed-" + randomUUID();

            cytomineSteps.createTag(wait, cytomineUrl, firstTagName);
            cytomineSteps.createTag(wait, cytomineUrl, secondTagName);
            try {
                cytomineSteps.sortTags(wait, cytomineUrl, firstTagName, secondTagName);
                cytomineSteps.changeTagsPerPage(wait, cytomineUrl, 10, firstTagName, secondTagName);

                cytomineSteps.editTag(wait, cytomineUrl, firstTagName, renamedTagName);
            } finally {
                cleanup(() -> cytomineSteps.deleteTag(wait, cytomineUrl, renamedTagName),
                    () -> cytomineSteps.deleteTag(wait, cytomineUrl, firstTagName),
                    () -> cytomineSteps.deleteTag(wait, cytomineUrl, secondTagName));
            }
        });
    }

    @Test
    void listProjects() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            Set<String> projectNames =
                Set.of("selenium-" + randomUUID(), "selenium-" + randomUUID(), "selenium-" + randomUUID());
            Set<String> projectUrls =
                projectNames.stream().map(name -> cytomineSteps.createProject(wait, driver, cytomineUrl, name))
                    .collect(toSet());
            cytomineSteps.listProjects(wait, cytomineUrl, projectNames);
            projectUrls.forEach(projectUrl -> {
                String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
                cytomineSteps.deleteProject(wait, projectUrl);
                cytomineSteps.deleteOntology(wait, ontologyUrl);
            });
        });
    }

    @Test
    void listImagesInProject() {
        multiUsers.run(wait, driver, List.of(ROLE_ADMIN, ROLE_USER), admin -> {
            Set<String> imageNames = Set.of("selenium-" + randomUUID() + ".png", "selenium-" + randomUUID() + ".png",
                "selenium-" + randomUUID() + ".png");
            imageNames.forEach(
                name -> cytomineSteps.addImage(wait, cytomineUrl, name, Optional.of(admin.projectName())));
            cytomineSteps.listImagesInProject(wait, admin.projectUrl(), imageNames);

            imageNames.forEach(imageName -> cytomineSteps.deleteImage(wait, cytomineUrl, imageName));
        });
    }

    @Test
    void addImageToStorageWithProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void addImageToStorageAndSort() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            Set<String> imageNames = Set.of("selenium-" + randomUUID() + ".png", "selenium-" + randomUUID() + ".png",
                "selenium-" + randomUUID() + ".png");

            imageNames.forEach(name -> cytomineSteps.addImage(wait, cytomineUrl, name, Optional.empty()));
            try {
                cytomineSteps.sortImagesInStorage(wait, cytomineUrl, imageNames);
            } finally {
                imageNames.forEach(name -> cleanup(() -> cytomineSteps.deleteImage(wait, cytomineUrl, name)));
            }
        });
    }

    @Test
    void addTermToOntology() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String termName = "selenium-term-" + randomUUID();
            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), termName);
            cytomineSteps.deleteTermFromOntology(wait, admin.ontologyUrl(), termName);

        });
    }

    @Test
    void deleteParentTermRemovesBothFromTree() {
        multiUsers.runAsAdmin(wait, driver, admin -> {

            String parentTermName = "selenium-parent-" + randomUUID();
            String childTermName = "selenium-child-" + randomUUID();

            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), parentTermName);
            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), childTermName);
            cytomineSteps.makeTermChildOf(wait, driver, admin.ontologyUrl(), childTermName, parentTermName);
            cytomineSteps.deleteTermFromOntology(wait, admin.ontologyUrl(), parentTermName);
            cytomineSteps.verifyTermsAbsentAfterRefresh(wait, admin.ontologyUrl(), parentTermName, childTermName);
        });
    }

    @Test
    void addAnnotationWithTools() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());

            annotationTools.drawPointAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            annotationTools.drawLineAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            annotationTools.drawFreeHandLineAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            annotationTools.drawCircleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            annotationTools.drawPolygonAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            annotationTools.drawFreeHandPolygonAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void addAnnotationWithTerm() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String termName = "selenium-term-" + randomUUID();

            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());

            cytomineSteps.selectTermForAnnotation(wait, termName);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void addAnnotationWithSam() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String termName = "selenium-term-" + randomUUID();

            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());
            cytomineSteps.selectTermForAnnotation(wait, termName);

            annotationTools.drawRectangleAnnotationWithMagicWand(wait, driver);
            cytomineSteps.verifyAnnotationProcessedWithSam(wait);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void uploadAndRunAndDeleteTaskRun() {
        String imageName = "selenium-" + randomUUID() + ".png";
        String zipName = "com.cytomine.dummy.identity.geometry-1.0.0.zip";
        String projectName = "selenium-" + randomUUID();
        String taskName = "identity with geometry";
        String taskVersion = "1.0.0";

        cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.uploadTask(wait, cytomineUrl, zipName);
        String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
        String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
        try {
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.selectTask(wait, taskName, taskVersion);
            cytomineSteps.selectAnnotationForGeometryInput(wait);
            cytomineSteps.runTask(wait, driver);
            cytomineSteps.deleteTaskRun(wait, projectUrl, taskName);
        } finally {
            cleanup(
                () -> cytomineSteps.deleteTask(wait, cytomineUrl, taskName),
                () -> cytomineSteps.deleteProject(wait, projectUrl),
                () -> cytomineSteps.deleteOntology(wait, ontologyUrl),
                () -> cytomineSteps.deleteImage(wait, cytomineUrl, imageName)
            );
        }
        cytomineSteps.logout(wait, cytomineUrl);
    }

    @Test
    void retrieveSimilarAnnotationWithCbir() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";

            String termName = "selenium-term-" + randomUUID();
            int nbAnnotations = 3;

            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());
            cytomineSteps.selectTermForAnnotation(wait, termName);

            for (int i = 0; i < nbAnnotations; i++) {
                annotationTools.drawRandomRectangleAnnotation(wait, driver);
                cytomineSteps.verifyAnnotationCreated(wait);
            }

            cytomineSteps.createAnnotationAndSearchAnnotations(wait, driver, nbAnnotations);
        });
    }

    @Test
    void addAndRemoveUserFromProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String username = "ImageServer1";
            cytomineSteps.addUserToProject(wait, admin.projectUrl(), username);
            cytomineSteps.changeUserRole(wait, admin.projectUrl(), username);
            cytomineSteps.removeUserFromProject(wait, admin.projectUrl(), username);
        });
    }

    @Test
    void filterProjectByName() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            int nbProjects = 3;
            String projectNameToSearch = "search-" + randomUUID();
            List<String> projectUrls = new ArrayList<>();
            List<String> projectNames = new ArrayList<>();

            projectUrls.add(cytomineSteps.createProject(wait, driver, cytomineUrl, projectNameToSearch));
            for (int i = 0; i < nbProjects; i++) {
                String projectName = "selenium-" + randomUUID();
                projectNames.add(projectName);
                projectUrls.add(cytomineSteps.createProject(wait, driver, cytomineUrl, projectName));
            }

            try {
                cytomineSteps.filterProjectByName(wait, cytomineUrl, projectNameToSearch, projectNames);
            } finally {
                for (String projectUrl : projectUrls) {
                    cleanup(() -> {
                        String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
                        cytomineSteps.deleteProject(wait, projectUrl);
                        cytomineSteps.deleteOntology(wait, ontologyUrl);
                    });
                }
            }
        });
    }

    @Test
    void filterAnnotationsByTermInProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String termName = "selenium-term-" + randomUUID();

            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());

            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);
            cytomineSteps.selectTermForAnnotation(wait, termName);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);
            sleep(1000);
            cytomineSteps.filterAnnotationsByTerm(wait, admin.projectUrl(), termName);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void downloadAnnotationReport() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.downloadAnnotationReport(wait, admin.projectUrl(), admin.projectName(), ReportType.PDF);
            cytomineSteps.downloadAnnotationReport(wait, admin.projectUrl(), admin.projectName(), ReportType.CSV);
            cytomineSteps.downloadAnnotationReport(wait, admin.projectUrl(), admin.projectName(), ReportType.Excel);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void exportAnnotations() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.exportAnnotations(wait, admin.projectUrl(), admin.projectName());

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void exportOntology() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String termName = "selenium-term-" + randomUUID();

            cytomineSteps.addTermToOntology(wait, driver, admin.ontologyUrl(), termName);

            cytomineSteps.exportOntology(wait, admin.ontologyUrl(), admin.ontologyName());

        });
    }

    @Test
    void seeRecentlyViewedProjectsInDashboard() {
        multiUsers.runAsAdmin(wait, driver, admin -> {

            String imageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());
            sleep(1000);
            cytomineSteps.checkRecentlyViewedProjects(wait, cytomineUrl, admin.projectName(), imageName);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void checkProjectAfterPimsImport() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "wsi";
            cytomineSteps.checkPimsImportProject(wait, cytomineUrl, admin.projectName(), imageName);
        });
    }

    @Test
    void reviewAnnotationsInProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {

            String imageName = "selenium-" + randomUUID() + ".png";

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());
            annotationTools.drawRectangleAnnotation(wait, driver);

            cytomineSteps.reviewAnnotations(wait);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void createAndDeleteImageGroup() {
        multiUsers.runAsAdmin(wait, driver, admin -> {

            String imageName = "selenium-" + randomUUID() + ".png";

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));

            String imageGroupName = "selenium-" + randomUUID();
            cytomineSteps.createImageGroup(wait, admin.projectUrl(), imageGroupName, Set.of(imageName));
            cytomineSteps.openImageGroupInViewer(wait, admin.projectUrl(), imageGroupName);
            cytomineSteps.deleteImageGroup(wait, admin.projectUrl(), imageGroupName);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void linkAnnotationsBetweenImages() {
        multiUsers.runAsAdmin(wait, driver, admin -> {

            String firstImageName = "selenium-" + randomUUID() + ".png";
            String secondImageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, firstImageName, Optional.of(admin.projectName()));
            cytomineSteps.addImage(wait, cytomineUrl, secondImageName, Optional.of(admin.projectName()));

            String imageGroupName = "selenium-" + randomUUID();
            cytomineSteps.createImageGroup(wait, admin.projectUrl(), imageGroupName,
                Set.of(firstImageName, secondImageName));
            cytomineSteps.openImageGroupInViewer(wait, admin.projectUrl(), imageGroupName);

            annotationTools.drawRectangleAnnotationInCell(wait, driver, 1);
            annotationTools.drawRectangleAnnotationInCell(wait, driver, 2);

            cytomineSteps.linkAnnotationToOtherView(wait);
            cytomineSteps.verifyLinkedAnnotationInDetails(wait);

            cytomineSteps.unlinkAnnotationFromView(wait);
            cytomineSteps.verifyAnnotationUnlinkedInDetails(wait);

            cytomineSteps.deleteImageGroup(wait, admin.projectUrl(), imageGroupName);
            cytomineSteps.deleteImage(wait, cytomineUrl, firstImageName);
            cytomineSteps.deleteImage(wait, cytomineUrl, secondImageName);
        });
    }

    @Test
    void undoCommand() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String renamedOntologyName = "selenium-renamed-" + randomUUID();

            cytomineSteps.renameOntology(wait, admin.ontologyUrl(), renamedOntologyName);

            cytomineSteps.undoCommandFromHistory(wait, cytomineUrl, "Update", renamedOntologyName);
            cytomineSteps.verifyOntologyName(wait, admin.ontologyUrl(), admin.ontologyName());

        });
    }

    @Test
    void screenshotInImageViewer() {
        multiUsers.runAsAdmin(wait, driver, admin -> {

            String imageName = "selenium-" + randomUUID() + ".png";

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.openImageInViewer(wait, admin.projectUrl());

            annotationTools.screenshotCurrentView(wait);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void addTagToImageInProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {

            String imageName = "selenium-" + randomUUID() + ".png";
            String tagName = "selenium-tag-" + randomUUID();

            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(admin.projectName()));
            cytomineSteps.createTag(wait, cytomineUrl, tagName);

            cytomineSteps.goToProjectTab(wait, admin.projectUrl(), "Images");
            cytomineSteps.addTagToImage(wait, imageName, tagName);

            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
            cytomineSteps.deleteTag(wait, cytomineUrl, tagName);
        });
    }

    @Test
    void login() {
        multiUsers.runAllRoles(wait, driver, user -> {
        });
    }

    @Test
    void createNewUserAndLoginAsUser() {
        multiUsers.run(wait, driver, List.of(ROLE_USER), user -> {
        });
    }

    @Test
    void createProjectAndOntologyAndImage() {
        multiUsers.run(wait, driver, List.of(ROLE_ADMIN, ROLE_USER), user -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.empty());
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void editUser() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String username = "selenium-user-" + randomUUID().toString().substring(0, 8);
            String firstname = "Selenium";
            String lastname = "User-" + randomUUID().toString().substring(0, 8);
            String email = username + "@selenium.test";
            String password = "Selenium1!";

            cytomineSteps.createUser(wait, cytomineUrl, username, firstname, lastname, email, password);
            try {
                cytomineSteps.editUser(wait, cytomineUrl, username, lastname, "UpdatedFirst", "UpdatedLast");
            } finally {
                cleanup(() -> keycloakClient.deleteUser(username));
            }
        });
    }

    @Test
    void deleteUser() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String username = "selenium-user-" + randomUUID().toString().substring(0, 8);
            String firstname = "Selenium";
            String lastname = "User-" + randomUUID().toString().substring(0, 8);
            String email = username + "@selenium.test";
            String password = "Selenium1!";

            cytomineSteps.createUser(wait, cytomineUrl, username, firstname, lastname, email, password);
            cytomineSteps.deleteUser(wait, cytomineUrl, username, lastname);
        });
    }
}
