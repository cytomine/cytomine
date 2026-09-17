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

@Import({
    AnnotationTools.class, CytomineSteps.class, KeycloakClient.class, MultiUsersRunner.class,
    SeleniumDriver.class, WebDriverUtils.class
})
@SpringBootTest
public class CytomineTests {
    @Autowired
    SeleniumDriver driverProvider;
    WebDriver driver;
    Wait<WebDriver> wait;

    @Value("${cytomine.url}")
    URL cytomineUrl;

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
        Screenshots.save(driver, "closing-" + testInfo.getTestMethod()
            .map(Method::getName)
            .orElseGet(() -> "no-name-" + randomUUID()));
        driver.quit();
    }

    @SneakyThrows
    private void sleep(long millis) {
        Thread.sleep(millis);
    }

    @Test
    void manageTagsInAdminPanel() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String firstTagName = "selenium-tag-a-" + randomUUID();
            String secondTagName = "selenium-tag-b-" + randomUUID();
            String renamedTagName = "selenium-tag-renamed-" + randomUUID();

            cytomineSteps.createTag(wait, cytomineUrl, firstTagName);
            cytomineSteps.createTag(wait, cytomineUrl, secondTagName);

            cytomineSteps.sortTags(wait, cytomineUrl, firstTagName, secondTagName);
            cytomineSteps.changeTagsPerPage(wait, cytomineUrl, 10, firstTagName, secondTagName);

            cytomineSteps.editTag(wait, cytomineUrl, firstTagName, renamedTagName);

            cytomineSteps.deleteTag(wait, cytomineUrl, renamedTagName);
            cytomineSteps.deleteTag(wait, cytomineUrl, secondTagName);
            cytomineSteps.logout(wait, cytomineUrl);
        });
    }

    @Test
    void listProjects() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            Set<String> projectNames = Set.of(
                "selenium-" + randomUUID(),
                "selenium-" + randomUUID(),
                "selenium-" + randomUUID()
            );
            Set<String> projectUrls = projectNames.stream()
                .map(name -> cytomineSteps.createProject(wait, driver, cytomineUrl, name))
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
        multiUsers.run(wait, driver, List.of(ROLE_ADMIN, ROLE_USER), user -> {
            Set<String> imageNames = Set.of(
                "selenium-" + randomUUID() + ".png",
                "selenium-" + randomUUID() + ".png",
                "selenium-" + randomUUID() + ".png"
            );
            String projectName = "selenium-" + randomUUID();

            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            imageNames.forEach(name -> cytomineSteps.addImage(wait, cytomineUrl, name, Optional.of(projectName)));

            cytomineSteps.listImagesInProject(wait, projectUrl, imageNames);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            imageNames.forEach(imageName -> cytomineSteps.deleteImage(wait, cytomineUrl, imageName));
        });
    }

    @Test
    void createAndDeleteOntology() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String ontologyName = "selenium-" + randomUUID();
            String ontologyURL = cytomineSteps.createOntology(wait, driver, cytomineUrl, ontologyName);
            cytomineSteps.deleteOntology(wait, ontologyURL);
        });
    }

    @Test
    void addImageToStorageWithProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void addImageToStorageAndSort() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            Set<String> imageNames = Set.of(
                "selenium-" + randomUUID() + ".png",
                "selenium-" + randomUUID() + ".png",
                "selenium-" + randomUUID() + ".png"
            );

            imageNames.forEach(name -> cytomineSteps.addImage(wait, cytomineUrl, name, Optional.empty()));

            cytomineSteps.sortImagesInStorage(wait, cytomineUrl, imageNames);

            imageNames.forEach(name -> cytomineSteps.deleteImage(wait, cytomineUrl, name));
        });
    }

    @Test
    void addTermToOntology() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String ontologyName = "selenium-ontology-" + randomUUID();
            String termName = "selenium-term-" + randomUUID();
            String ontologyURL = cytomineSteps.createOntology(wait, driver, cytomineUrl, ontologyName);
            cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
            cytomineSteps.deleteTermFromOntology(wait, ontologyURL, termName);
            cytomineSteps.deleteOntology(wait, ontologyURL);
        });
    }

    @Test
    void deleteParentTermRemovesBothFromTree() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String ontologyName = "selenium-ontology-" + randomUUID();
            String parentTermName = "selenium-parent-" + randomUUID();
            String childTermName = "selenium-child-" + randomUUID();
            String ontologyURL = cytomineSteps.createOntology(wait, driver, cytomineUrl, ontologyName);
            cytomineSteps.addTermToOntology(wait, driver, ontologyURL, parentTermName);
            cytomineSteps.addTermToOntology(wait, driver, ontologyURL, childTermName);
            cytomineSteps.makeTermChildOf(wait, driver, ontologyURL, childTermName, parentTermName);
            cytomineSteps.deleteTermFromOntology(wait, ontologyURL, parentTermName);
            cytomineSteps.verifyTermsAbsentAfterRefresh(wait, ontologyURL, parentTermName, childTermName);
            cytomineSteps.deleteOntology(wait, ontologyURL);
        });
    }

    @Test
    void addAnnotationWithTools() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);

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

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void addAnnotationWithTerm() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();
            String termName = "selenium-term-" + randomUUID();

            String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
            cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectURL);

            cytomineSteps.selectTermForAnnotation(wait, termName);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.deleteProject(wait, projectURL);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
            cytomineSteps.deleteOntology(wait, ontologyURL);
        });
    }

    @Test
    void addAnnotationWithSam() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();
            String termName = "selenium-term-" + randomUUID();

            String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
            cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectURL);
            cytomineSteps.selectTermForAnnotation(wait, termName);

            annotationTools.drawRectangleAnnotationWithMagicWand(wait, driver);
            cytomineSteps.verifyAnnotationProcessedWithSam(wait);

            cytomineSteps.deleteProject(wait, projectURL);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
            cytomineSteps.deleteOntology(wait, ontologyURL);
        });
    }

    @Test
    void uploadAndDeleteTask() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String zipName = "com.cytomine.dummy.identity.image-1.0.0.zip";

            cytomineSteps.uploadTask(wait, cytomineUrl, zipName);
            cytomineSteps.deleteTask(wait, cytomineUrl, "identity with image");
        });
    }

    @Test
    void runTaskAndDeleteRun() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String zipName = "com.cytomine.dummy.identity.geometry-1.0.0.zip";
            String projectName = "selenium-" + randomUUID();
            String taskName = "identity with geometry";
            String taskVersion = "1.0.0";

            cytomineSteps.uploadTask(wait, cytomineUrl, zipName);
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.selectTask(wait, taskName, taskVersion);
            cytomineSteps.selectAnnotationForGeometryInput(wait);
            cytomineSteps.runTask(wait, driver);
            cytomineSteps.deleteTaskRun(wait, projectUrl, taskName);

            cytomineSteps.deleteTask(wait, cytomineUrl, taskName);
            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void retrieveSimilarAnnotationWithCbir() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();
            String termName = "selenium-term-" + randomUUID();
            int nbAnnotations = 3;

            String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
            cytomineSteps.addTermToOntology(wait, driver, ontologyURL, termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectURL);
            cytomineSteps.selectTermForAnnotation(wait, termName);

            for (int i = 0; i < nbAnnotations; i++) {
                annotationTools.drawRandomRectangleAnnotation(wait, driver);
                cytomineSteps.verifyAnnotationCreated(wait);
            }

            cytomineSteps.createAnnotationAndSearchAnnotations(wait, driver, nbAnnotations);

            cytomineSteps.deleteProject(wait, projectURL);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
            cytomineSteps.deleteOntology(wait, ontologyURL);
        });
    }

    @Test
    void addUserToProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, "selenium-" + randomUUID());
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);

            cytomineSteps.addUserToProject(wait, projectUrl, "ImageServer1");

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
        });
    }

    @Test
    void removeUserFromProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String username = "ImageServer1";
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, "selenium-" + randomUUID());
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            cytomineSteps.addUserToProject(wait, projectUrl, username);

            cytomineSteps.removeUserFromProject(wait, projectUrl, username);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
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

            cytomineSteps.filterProjectByName(wait, cytomineUrl, projectNameToSearch, projectNames);

            for (String projectUrl : projectUrls) {
                String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
                cytomineSteps.deleteProject(wait, projectUrl);
                cytomineSteps.deleteOntology(wait, ontologyUrl);
            }
        });
    }

    @Test
    void filterAnnotationsByTermInProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            String termName = "selenium-term-" + randomUUID();
            cytomineSteps.addTermToOntology(wait, driver, ontologyUrl, termName);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);

            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);
            cytomineSteps.selectTermForAnnotation(wait, termName);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);
            sleep(1000);
            cytomineSteps.filterAnnotationsByTerm(wait, projectUrl, termName);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void downloadAnnotationReport() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();

            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.downloadAnnotationReport(wait, projectUrl, projectName, ReportType.PDF);
            cytomineSteps.downloadAnnotationReport(wait, projectUrl, projectName, ReportType.CSV);
            cytomineSteps.downloadAnnotationReport(wait, projectUrl, projectName, ReportType.Excel);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void exportAnnotations() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String imageName = "selenium-" + randomUUID() + ".png";
            String projectName = "selenium-" + randomUUID();

            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);
            annotationTools.drawRectangleAnnotation(wait, driver);
            cytomineSteps.verifyAnnotationCreated(wait);

            cytomineSteps.exportAnnotations(wait, projectUrl, projectName);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void exportOntology() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String ontologyName = "selenium-" + randomUUID();
            String termName = "selenium-term-" + randomUUID();

            String ontologyUrl = cytomineSteps.createOntology(wait, driver, cytomineUrl, ontologyName);
            cytomineSteps.addTermToOntology(wait, driver, ontologyUrl, termName);

            cytomineSteps.exportOntology(wait, ontologyUrl, ontologyName);

            cytomineSteps.deleteOntology(wait, ontologyUrl);
        });
    }

    @Test
    void seeRecentlyViewedProjectsInDashboard() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            String imageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);
            sleep(1000);
            cytomineSteps.checkRecentlyViewedProjects(wait, cytomineUrl, projectName, imageName);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void checkProjectAfterPimsImport() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String projectName = "test-project";
            String imageName = "wsi";

            cytomineSteps.checkPimsImportProject(wait, cytomineUrl, projectName, imageName);
        });
    }

    @Test
    void reviewAnnotationsInProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            String imageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);
            annotationTools.drawRectangleAnnotation(wait, driver);

            cytomineSteps.reviewAnnotations(wait);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void createAndDeleteImageGroup() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            String imageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));

            String imageGroupName = "selenium-" + randomUUID();
            cytomineSteps.createImageGroup(wait, projectUrl, imageGroupName, Set.of(imageName));
            cytomineSteps.openImageGroupInViewer(wait, projectUrl, imageGroupName);
            cytomineSteps.deleteImageGroup(wait, projectUrl, imageGroupName);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void undoCommand() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String ontologyName = "selenium-" + randomUUID();
            String renamedOntologyName = "selenium-renamed-" + randomUUID();

            String ontologyUrl = cytomineSteps.createOntology(wait, driver, cytomineUrl, ontologyName);
            cytomineSteps.renameOntology(wait, ontologyUrl, renamedOntologyName);

            cytomineSteps.undoCommandFromHistory(wait, cytomineUrl, "Update", renamedOntologyName);
            cytomineSteps.verifyOntologyName(wait, ontologyUrl, ontologyName);

            cytomineSteps.deleteOntology(wait, ontologyUrl);
        });
    }

    @Test
    void screenshotInImageViewer() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            String imageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            cytomineSteps.openImageInViewer(wait, projectUrl);

            annotationTools.screenshotCurrentView(wait);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
        });
    }

    @Test
    void addTagToImageInProject() {
        multiUsers.runAsAdmin(wait, driver, admin -> {
            String projectName = "selenium-" + randomUUID();
            String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);
            String imageName = "selenium-" + randomUUID() + ".png";
            cytomineSteps.addImage(wait, cytomineUrl, imageName, Optional.of(projectName));
            String tagName = "selenium-tag-" + randomUUID();
            cytomineSteps.createTag(wait, cytomineUrl, tagName);

            cytomineSteps.goToProjectTab(wait, projectUrl, "Images");
            cytomineSteps.addTagToImage(wait, imageName, tagName);

            cytomineSteps.deleteProject(wait, projectUrl);
            cytomineSteps.deleteOntology(wait, ontologyUrl);
            cytomineSteps.deleteImage(wait, cytomineUrl, imageName);
            cytomineSteps.deleteTag(wait, cytomineUrl, tagName);
        });
    }
    
    @Test
    void createProjectAndOntologyAndImage() {
        multiUsers.run(wait, driver, List.of(ROLE_ADMIN, ROLE_USER), user -> {
            String projectName = "selenium-" + randomUUID();
            String imageName = "selenium-" + randomUUID() + ".png";

            String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
            String ontologyURL = cytomineSteps.getOntologyUrlFromProject(wait, projectURL);
            cytomineSteps.deleteProject(wait, projectURL);
            cytomineSteps.deleteOntology(wait, ontologyURL);

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
            cytomineSteps.editUser(wait, cytomineUrl, username, lastname, "UpdatedFirst", "UpdatedLast");
            keycloakClient.deleteUser(username);
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
