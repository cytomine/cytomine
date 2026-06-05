package org.cytomine.e2etests.ui;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.SneakyThrows;
import org.cytomine.e2etests.utils.ReportType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.cytomine.e2etests.configuration.SeleniumDriver.DOWNLOAD_PATH;

@Component
public class CytomineSteps {

    @Autowired
    AnnotationTools annotationTools;

    @Autowired
    WebDriverUtils webDriverUtils;

    public void login(Wait<WebDriver> wait, URL cytomineUrl, String username, String password) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.byIsDisplayed(wait, By.id("username"));
        webDriverUtils.bySendKeys(wait, By.id("username"), username);
        webDriverUtils.bySendKeys(wait, By.id("password"), password);
        webDriverUtils.byClick(wait, By.cssSelector("input[type='submit']"));
        webDriverUtils.byIsDisplayed(wait, By.id("app"));
    }

    public void logout(Wait<WebDriver> wait, URL cytomineUrl) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.byClick(wait, By.cssSelector(".navbar-end .navbar-item.has-dropdown"));
        webDriverUtils.byClick(wait, By.cssSelector(".navbar-item .fa-power-off"));
        webDriverUtils.byIsDisplayed(wait, By.id("username"));
    }

    @SneakyThrows
    public String createProject(Wait<WebDriver> wait, WebDriver driver, URL cytomineUrl, String projectName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.xpathClick(wait, "//a[@href='#/projects']");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'New project')]");
        webDriverUtils.bySendKeys(wait, By.name("name"), projectName);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//h1[contains(text(), 'Project: ')]"));
        return driver.getCurrentUrl();
    }

    public void deleteProject(Wait<WebDriver> wait, String projectURL) {
        webDriverUtils.goTo(wait, projectURL);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Delete')]");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'successfully deleted')]"));
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//div[contains(text(), 'successfully deleted')]"));
    }

    public void listProjects(Wait<WebDriver> wait, URL cytomineUrl, Set<String> projectNames) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.xpathClick(wait, "//a[@href='#/projects']");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'New project')]"));
        projectNames.forEach(
            name -> webDriverUtils.byIsDisplayed(wait, By.xpath(format("//a[contains(text(), '%s')]", name)))
        );
    }

    public void listImagesInProject(Wait<WebDriver> wait, String projectUrl, Set<String> imageNames) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "images"));
        imageNames.forEach(
            name -> webDriverUtils.byIsDisplayed(wait, By.xpath(format("//a[span[contains(text(), '%s')]]", name)))
        );
    }

    public String createOntology(Wait<WebDriver> wait, WebDriver driver, URL cytomineUrl, String ontologyName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.xpathClick(wait, "//a[@href='#/ontology']");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'New ontology')]");
        webDriverUtils.bySendKeys(wait, By.name("name"), ontologyName);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//p[contains(@class, 'panel-heading') and contains(text(), '" + ontologyName + "')]")
        );
        return driver.getCurrentUrl();
    }

    public void deleteOntology(Wait<WebDriver> wait, String ontologyURL) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Delete')]");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'successfully deleted')]"));
    }

    public String getOntologyUrlFromProject(Wait<WebDriver> wait, String projectURL) {
        webDriverUtils.goTo(wait, projectURL);
        webDriverUtils.xpathClick(wait, "//a[contains(@href, '/information')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//td[contains(text(), 'Ontology')]"));
        return wait.until(d -> {
            var elements = d.findElements(By.xpath("//a[contains(@href, '/ontology/')]"));
            return elements.getFirst().getAttribute("href");
        });
    }

    @SneakyThrows
    public void addImage(Wait<WebDriver> wait, URL cytomineUrl, String imageName, Optional<String> maybeProjectName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/storage");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//a[contains(., 'Add files')]"));

        Path tempDir = Files.createTempDirectory("selenium-upload");
        Path copiedFile = tempDir.resolve(imageName);
        try (var in = getClass().getClassLoader().getResourceAsStream("cat.png")) {
            Files.copy(in, copiedFile);
        }
        maybeProjectName.ifPresent(projectName -> selectProject(wait, projectName));

        webDriverUtils.bySendKeysWait(wait, By.cssSelector("input[type='file']"), copiedFile.toString(), false);
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'Start upload') and not(@disabled)]"));
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Start upload')]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//div[contains(@class,'uploaded-files-list')]//*[contains(text(),'" + imageName + "')]")
        );
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath(
                "//tr[.//td[@data-label='Filename']//span[normalize-space(.)='" + imageName + "']]"
                    + "//td[@data-label='Status']//span[@data-status='success']"
            )
        );
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'Hide successful uploads')]"));
        webDriverUtils.byClick(wait, By.xpath("//button[contains(text(), 'Hide successful uploads')]"));
    }

    private void selectProject(Wait<WebDriver> wait, String projectName) {
        webDriverUtils.byClick(wait, By.cssSelector(".project-select .multiselect__tags"));

        By selectedOption = By.xpath(
            "//div[contains(@class,'project-select')]"
                + "//span[contains(@class,'multiselect__option--selected')]"
                + "/span[@data-option='" + projectName + "']"
        );
        if (webDriverUtils.isAbsent(wait, selectedOption)) {
            webDriverUtils.xpathClick(wait, "//span[@data-option='" + projectName + "']");
        } else {
            webDriverUtils.byClick(wait, By.cssSelector(".project-select .multiselect__select"));
        }

        webDriverUtils.waitUntilByEmpty(
            wait,
            By.xpath("//div[contains(@class,'project-select')]//input[@placeholder='Select options']")
        );
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath(
                "//div[contains(@class,'project-select')]"
                    + "//div[@class='multiselect__tags-wrap']"
                    + "[.//strong[normalize-space()='All'] or .//span[normalize-space()='" + projectName + "']]"
            )
        );
    }

    public void deleteImage(Wait<WebDriver> wait, URL cytomineUrl, String imageName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/storage");
        webDriverUtils.waitLoading(wait);
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//div[contains(@class,'uploaded-files-list')]//span[@data-filename='" + imageName + "']")
        );
        webDriverUtils.byClick(
            wait,
            By.xpath("//div[contains(@class,'uploaded-files-list')]//button[@data-filename='" + imageName + "']")
        );
        By confirmButton = By.xpath(
            "//footer[contains(@class,'modal-card-foot')]"
                + "//button[contains(@class,'is-danger') and normalize-space()='Confirm']"
        );
        webDriverUtils.byClick(wait, confirmButton);
        webDriverUtils.waitUntilByEmpty(
            wait,
            By.xpath("//div[contains(@class,'uploaded-files-list')]//span[@data-filename='" + imageName + "']")
        );
    }

    public String addTermToOntology(Wait<WebDriver> wait, WebDriver driver, String ontologyURL, String termName) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Add a term')]");
        webDriverUtils.bySendKeys(wait, By.name("name"), termName);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//span[contains(@class, 'ontology-term') and contains(text(), '" + termName + "')]")
        );
        return driver.getCurrentUrl();
    }

    public void deleteTermFromOntology(Wait<WebDriver> wait, String ontologyURL, String termName) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.xpathClick(wait,
            "//span[contains(@class, 'ontology-term') and contains(text(), '" + termName + "')]");
        webDriverUtils.xpathClick(wait,
            "//button[contains(@data-delete-term, '" + termName + "')]");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.waitUntilByEmpty(wait, By.xpath(
            "//span[contains(@class, 'ontology-term') and contains(text(), '" + termName + "')]"));
    }

    public void makeTermChildOf(Wait<WebDriver> wait, WebDriver driver, String ontologyURL,
                                String childTermName, String parentTermName) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.waitLoading(wait);
        var source = wait.until(d -> d.findElement(By.xpath(
            "//span[contains(@class, 'ontology-term') and contains(text(), '" + childTermName + "')]")));
        var target = wait.until(d -> d.findElement(By.xpath(
            "//span[contains(@class, 'ontology-term') and contains(text(), '" + parentTermName + "')]")));
        new Actions(driver)
            .clickAndHold(source)
            .moveByOffset(1, 1)
            .moveToElement(target)
            .release()
            .perform();
        webDriverUtils.waitLoading(wait);
        webDriverUtils.byIsDisplayed(wait, By.xpath(
            "//span[contains(@class, 'ontology-term') and contains(text(), '" + childTermName + "')]"));
    }

    public void verifyTermsAbsentAfterRefresh(Wait<WebDriver> wait, String ontologyURL, String... termNames) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.waitLoading(wait);
        for (String termName : termNames) {
            webDriverUtils.waitUntilByEmpty(
                wait,
                By.xpath("//span[contains(@class, 'ontology-term') and contains(text(), '" + termName + "')]")
            );
        }
    }

    public void openImageInViewer(Wait<WebDriver> wait, String projectURL) {
        webDriverUtils.goTo(wait, projectURL);
        webDriverUtils.xpathClick(wait, "//li//a[.//i[contains(@class, 'fa-image')]]");
        webDriverUtils.waitLoading(wait);
        webDriverUtils.byIsDisplayed(wait, By.xpath("//td//a[contains(@href, '/image/')]"));
        webDriverUtils.xpathClick(wait, "//td//a[contains(@href, '/image/')]");
        webDriverUtils.waitLoading(wait);
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".draw-tools-wrapper"));
    }

    public void selectTermForAnnotation(Wait<WebDriver> wait, String termName) {
        webDriverUtils.xpathClick(wait, "//div[contains(@class, 'term-selection')]//button");
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".ontology-tree-container"));
        webDriverUtils.xpathClick(wait,
            "//div[contains(@class, 'ontology-tree-container')]//div[contains(@class, "
                + "'tree-selector')]"
                +
                "[.//span[contains(@class, 'ontology-term') and contains(text(), '" + termName
                + "')]]");
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".term-selection .color-preview"));
        webDriverUtils.xpathClick(wait, "//div[contains(@class, 'term-selection')]//button");
    }

    public void verifyAnnotationCreated(Wait<WebDriver> wait) {
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".draw-tools-wrapper"));
        webDriverUtils.xpathClick(wait, "//button[.//i[contains(@class, 'fa-mouse-pointer')]]");
        webDriverUtils.byIsDisplayed(wait, By.xpath(
            "//button[contains(@class, 'is-selected') and .//i[contains(@class, "
                + "'fa-mouse-pointer')]]"));
    }

    public void verifyAnnotationProcessedWithSam(Wait<WebDriver> wait) {
        verifyAnnotationCreated(wait);
        webDriverUtils.byIsDisplayed(wait, By.xpath("//*[contains(text(),'Successful SAM Processing !')]"));
    }

    public void createAnnotationAndSearchAnnotations(Wait<WebDriver> wait, WebDriver driver, int nbAnnotations) {
        annotationTools.drawRectangleAnnotation(wait, driver);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Search for similar annotations')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//*[contains(text(), 'Similar annotations')]"));
        By annotationsSelector = By.cssSelector(".similar-annotations-playground .annotation-data");
        wait.until(d -> d.findElements(annotationsSelector).size() == nbAnnotations);
    }

    @SneakyThrows
    public void uploadTask(Wait<WebDriver> wait, URL cytomineUrl, String zipName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/apps");
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".upload-icon"));
        String bundleName = "selenium-" + UUID.randomUUID() + ".zip";
        Path tempDir = Files.createTempDirectory("selenium-task");
        Path copiedFile = tempDir.resolve(bundleName);
        try (var in = getClass().getClassLoader().getResourceAsStream(zipName)) {
            Files.copy(in, copiedFile);
        }

        webDriverUtils.bySendKeysWait(wait, By.cssSelector("input[type='file']"), copiedFile.toString(), false);
        webDriverUtils.xpathClick(wait, "//button[contains(., 'Upload')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//*[contains(text(),'Upload completed')]"));
    }

    public void deleteTask(Wait<WebDriver> wait, URL cytomineUrl, String taskName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/apps");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//p[contains(@class, 'title') and contains(text(), '" + taskName + "')]")
        );
        webDriverUtils.xpathClick(
            wait,
            "//div[contains(@class, 'card') and .//p[contains(@class, 'title') and contains(text(), '" + taskName
                + "')]]//a[contains(text(), 'More')]"
        );

        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".panel-heading .panel-actions"));
        webDriverUtils.byClick(wait, By.cssSelector(".panel-actions .dropdown .icon"));

        webDriverUtils.xpathClick(
            wait,
            "//a[contains(@class, 'dropdown-item') and .//span[contains(text(), 'Delete')]]"
        );

        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");

        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'App deleted successfully')]"));
    }

    public void selectTask(Wait<WebDriver> wait, String taskName, String taskVersion) {
        By appButton = By.xpath("//button[contains(@class,'button')][.//i[contains(@class,'fa-chevron-up')]]");
        webDriverUtils.byClick(wait, appButton);

        String optionXpath = format(
            "//option[contains(text(), '%s') and contains(text(), '%s')]",
            taskName,
            taskVersion
        );
        webDriverUtils.xpathClick(wait, optionXpath);
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath(format(
                "//div[contains(@class,'selected-app-card')]//strong[contains(text(),'%s') and contains(text(),'%s')]",
                taskName,
                taskVersion
            ))
        );
    }

    public void selectAnnotationForGeometryInput(Wait<WebDriver> wait) {
        webDriverUtils.xpathClick(
            wait,
            "//div[contains(@class,'field')][.//span[.=' input ']]//button[.//span[.=' Select ']]"
        );
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".modal-card .annotation-content"));
        wait.until(d -> !d.findElements(By.cssSelector(".annotation-content > div")).isEmpty());

        webDriverUtils.byClick(wait, By.cssSelector(".annotation-content > div:first-child"));
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".annotation-content > div.selected"));

        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".modal-card .modal-card-foot .is-link"));
        webDriverUtils.byClick(wait, By.cssSelector(".modal-card .modal-card-foot .is-link"));

        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//div[contains(@class, 'annotation-container') and contains(text(), 'Annotation')]")
        );
    }

    public void runTask(Wait<WebDriver> wait, WebDriver driver) {
        webDriverUtils.xpathClick(
            wait,
            "//button[contains(@class,'start-button')][.//span[.=' Run Task ']]"
        );

        Wait<WebDriver> longWait = new WebDriverWait(driver, Duration.ofSeconds(300));
        webDriverUtils.byIsDisplayed(longWait, By.cssSelector(".is-success .fa-check-circle"));
    }

    public void deleteTaskRun(Wait<WebDriver> wait, String projectUrl, String taskName) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "apps"));
        webDriverUtils.byClick(wait, By.cssSelector("table tbody tr:first-child button.is-danger"));
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//*[contains(text(),'" + taskName + "')]"));
    }

    public void addUserToProject(Wait<WebDriver> wait, String projectUrl, String username) {
        webDriverUtils.goTo(wait, projectUrl);
        webDriverUtils.byClick(wait, By.xpath("//label[contains(@class,'b-radio') and contains(text(),'Members')]"));
        webDriverUtils.byClick(wait, By.cssSelector("button.button.is-link.add-member"));
        By searchInput = By.xpath("//input[@placeholder='Search user...']");
        webDriverUtils.byClick(wait, searchInput);
        webDriverUtils.bySendKeys(wait, searchInput, username);
        webDriverUtils.byHitEnter(wait, searchInput);
        webDriverUtils.byClick(
            wait,
            By.xpath(
                "//button[contains(@class,'button') "
                    + "and contains(@class,'is-link') "
                    + "and normalize-space(text())='Add']"
            )
        );
        webDriverUtils.byIsDisplayed(wait, By.xpath("//*[contains(text(),'" + username + "')]"));
    }

    public void removeUserFromProject(Wait<WebDriver> wait, String projectUrl, String username) {
        webDriverUtils.goTo(wait, projectUrl);
        webDriverUtils.byClick(wait, By.xpath("//label[contains(@class,'b-radio') and contains(text(),'Members')]"));
        webDriverUtils.byClick(
            wait,
            By.xpath("//td[@data-label='Username' and normalize-space(text())='" + username + "']"
                + "/preceding-sibling::td[contains(@class,'checkbox-cell')]//label[contains(@class,'b-checkbox')]")
        );
        webDriverUtils.byClick(
            wait,
            By.xpath("//button[contains(@class,'is-danger') and normalize-space(text())='Remove selected members']")
        );
        webDriverUtils.byClick(
            wait,
            By.xpath("//button[contains(@class,'is-danger') and normalize-space(text())='Confirm']")
        );
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//*[contains(text(),'" + username + "')]"));
    }

    public void filterProjectByName(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String projectNameToSearch,
        List<String> projectNames
    ) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/projects");
        By searchInput = By.cssSelector("div.search-projects input[type='search']");
        webDriverUtils.byClick(wait, searchInput);
        webDriverUtils.bySendKeys(wait, searchInput, projectNameToSearch);

        projectNames.forEach(projectName ->
            webDriverUtils.waitUntilByEmpty(
                wait,
                By.xpath("//a[normalize-space(text())='" + projectName + "']")
            )
        );
        webDriverUtils.byIsDisplayed(wait, By.xpath("//a[normalize-space(text())='" + projectNameToSearch + "']"));
        webDriverUtils.byClear(wait, searchInput);
    }

    public void filterAnnotationsByTerm(Wait<WebDriver> wait, String projectUrl, String termName) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "annotations"));

        By termsMultiselect = By.xpath(
            "//div[contains(@class,'filter')]"
                + "[.//div[contains(@class,'filter-label') and normalize-space()='Terms']]"
                + "/div[contains(@class,'filter-body')]"
                + "//div[contains(@class,'multiselect__select')]"
        );
        webDriverUtils.byClick(wait, termsMultiselect);

        By selectAll = By.xpath(
            "//div[contains(@class,'filter')]"
                + "[.//div[contains(@class,'filter-label') and normalize-space()='Terms']]"
                + "//li[contains(@class,'multiselect__select-all')]"
        );
        webDriverUtils.byClick(wait, selectAll);

        By testTerm = By.xpath(
            "//span[contains(@class,'ontology-term') and normalize-space()='" + termName + "']"
        );
        webDriverUtils.byClick(wait, testTerm);

        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//h2[span[normalize-space(text())='" + termName + "'] and contains(., '(1)')]")
        );
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//div[contains(text(), 'NO TERM')]"));
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//div[contains(text(), 'MULTIPLE TERMS')]"));
    }

    public void downloadAnnotationReport(
        Wait<WebDriver> wait,
        String projectUrl,
        String projectName,
        ReportType format
    ) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "annotations"));
        webDriverUtils.byClick(
            wait,
            By.xpath("//button[normalize-space()='Download " + format + "']")
        );

        String filenameSuffix = "_" + projectName + "_annotations." + format.getLabel();
        Instant end = Instant.now().plus(Duration.ofSeconds(5));

        while (Instant.now().isBefore(end)) {
            File directory = new File(DOWNLOAD_PATH);
            File[] matches = directory.listFiles((d, name) -> name.endsWith(filenameSuffix));
            if (matches != null && matches.length > 0 && matches[0].length() > 0) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new RuntimeException("File ending with " + filenameSuffix + " was not found!");
    }

    public void exportAnnotations(Wait<WebDriver> wait, String projectUrl, String projectName) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "annotations"));
        webDriverUtils.byClick(wait, By.xpath("//button[normalize-space()='Export annotations']"));

        String filenameSuffix = "_" + projectName + "_annotations." + ReportType.GEOJSON.getLabel();
        Instant end = Instant.now().plus(Duration.ofSeconds(5));

        while (Instant.now().isBefore(end)) {
            File directory = new File(DOWNLOAD_PATH);
            File[] matches = directory.listFiles((d, name) -> name.endsWith(filenameSuffix));
            if (matches != null && matches.length > 0 && matches[0].length() > 0) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new RuntimeException("File ending with " + filenameSuffix + " was not found!");
    }

    public void exportOntology(Wait<WebDriver> wait, String ontologyUrl, String ontologyName) {
        webDriverUtils.goTo(wait, ontologyUrl);
        webDriverUtils.byClick(wait, By.xpath("//button[normalize-space()='Export ontology']"));

        String filename = ontologyName + "." + ReportType.JSON.getLabel();
        Instant end = Instant.now().plus(Duration.ofSeconds(5));

        while (Instant.now().isBefore(end)) {
            File directory = new File(DOWNLOAD_PATH);
            File[] matches = directory.listFiles((d, name) -> name.equals(filename));
            if (matches != null && matches.length > 0 && matches[0].length() > 0) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new RuntimeException("Ontology with name " + filename + " was not found!");
    }

    public void createUser(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String username,
        String firstname,
        String lastname,
        String email,
        String password
    ) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/admin?tab=users");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'New user')]"));
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'New user')]");
        webDriverUtils.byIsDisplayed(wait, By.name("username"));
        webDriverUtils.bySendKeys(wait, By.name("username"), username);
        webDriverUtils.bySendKeys(wait, By.name("firstname"), firstname);
        webDriverUtils.bySendKeys(wait, By.name("lastname"), lastname);
        webDriverUtils.bySendKeys(wait, By.name("email"), email);
        webDriverUtils.bySendKeys(wait, By.name("password"), password);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'User successfully created')]"));
        webDriverUtils.byIsDisplayed(wait, By.xpath("//td[normalize-space(text())='" + username + "']"));
    }

    public void editUser(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String username,
        String newFirstname,
        String newLastname
    ) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/admin?tab=users");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'New user')]"));
        webDriverUtils.xpathClick(
            wait,
            "//tr[.//td[normalize-space(text())='" + username + "']]//button[contains(text(), 'Edit')]"
        );
        webDriverUtils.byIsDisplayed(wait, By.name("firstname"));
        webDriverUtils.byClear(wait, By.name("firstname"));
        webDriverUtils.bySendKeys(wait, By.name("firstname"), newFirstname);
        webDriverUtils.byClear(wait, By.name("lastname"));
        webDriverUtils.bySendKeys(wait, By.name("lastname"), newLastname);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'User successfully updated')]"));
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//td[contains(normalize-space(text()), '" + newFirstname + " " + newLastname + "')]")
        );
    }

    public void checkRecentlyViewedProjects(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String projectName,
        String imageName
    ) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), " + projectName + ")]"));
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), " + imageName + ")]"));
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//div[contains(text(), 'No project recently opened')]"));
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//div[contains(text(), 'No image recently opened')]"));
    }

    public void checkPimsImportProject(
        Wait<WebDriver> wait,
        URL cytomineUrl,
        String projectName,
        String imageName
    ) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/projects");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), " + projectName + ")]"));
        webDriverUtils.xpathClick(wait, "//a[contains(text(), '" + projectName + "')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), " + imageName + ")]"));
    }

    public void reviewAnnotations(Wait<WebDriver> wait) {
        By validateButton = By.xpath(
            "//div[contains(@class,'buttons') and contains(@class,'has-addons')]"
                + "//button[contains(@class,'is-success')][.//i[contains(@class,'fa-check')]]"
        );
        By rejectButton = By.xpath(
            "//div[contains(@class,'buttons') and contains(@class,'has-addons')]"
                + "//button[@disabled][.//i[contains(@class,'fa-minus')]]"
        );
        webDriverUtils.waitUntilByEmpty(wait, validateButton);
        webDriverUtils.waitUntilByEmpty(wait, rejectButton);

        webDriverUtils.byClick(wait, By.cssSelector(".fa-check-circle"));
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//a[contains(@class,'active')][.//i[contains(@class,'fa-check-circle')]]")
        );

        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Start review']]");
        webDriverUtils.byIsDisplayed(wait, validateButton);
        webDriverUtils.byIsDisplayed(wait, rejectButton);

        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Accept all']]");
        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Reject all']]");
        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Continue my review later']]");
        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Continue review']]");

        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Cancel my review']]");

        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Start review']]");
        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Validate my review']]");

        webDriverUtils.xpathClick(wait, "//button[.//span[normalize-space()='Unvalidate and continue my review']]");
    }

    public void createImageGroup(
        Wait<WebDriver> wait,
        String projectUrl,
        String imageGroupName,
        Set<String> imageNames
    ) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "image-groups"));
        webDriverUtils.xpathClick(wait, "//button[normalize-space()='Add image group']");
        webDriverUtils.bySendKeys(wait, By.cssSelector("input[name='name']"), imageGroupName);
        webDriverUtils.xpathClick(wait, "//button[normalize-space()='Save']");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'Image group successfully created')]"));
        webDriverUtils.waitUntilByEmpty(wait, By.xpath("//div[contains(text(), 'Image group successfully created')]"));

        for (String imageName : imageNames) {
            webDriverUtils.xpathClick(
                wait,
                "//tr[td[normalize-space()='" + imageName + "']]//button[normalize-space()='Add']"
            );
        }

        webDriverUtils.xpathClick(wait, "//button[normalize-space()='Close']");
    }

    public void deleteImageGroup(
        Wait<WebDriver> wait,
        String projectUrl,
        String imageGroupName
    ) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "image-groups"));
        webDriverUtils.xpathClick(
            wait,
            "//tr[td//a[normalize-space()='" + imageGroupName + "']]"
                + "//td[contains(@class,'chevron-cell')]//a[@role='button']"
        );
        webDriverUtils.xpathClick(wait, "//button[normalize-space()='Delete']");
        webDriverUtils.xpathClick(wait, "//button[normalize-space()='Confirm']");

        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//div[contains(text(), 'Image group " + imageGroupName + " was successfully deleted')]")
        );
        webDriverUtils.waitUntilByEmpty(
            wait,
            By.xpath("//div[contains(text(), 'Image group " + imageGroupName + " was successfully deleted')]")
        );
    }

    public void openImageGroupInViewer(
        Wait<WebDriver> wait,
        String projectUrl,
        String imageGroupName
    ) {
        webDriverUtils.goTo(wait, projectUrl.replace("configuration", "image-groups"));
        webDriverUtils.xpathClick(wait, "//a[normalize-space()='" + imageGroupName + "']");
        webDriverUtils.waitLoading(wait);
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".draw-tools-wrapper"));
    }
}
