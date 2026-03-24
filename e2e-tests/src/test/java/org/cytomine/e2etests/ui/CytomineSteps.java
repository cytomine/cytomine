package org.cytomine.e2etests.ui;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

@Component
public class CytomineSteps {

    public static final int DEFAULT_CANVA_OFFSET = 2;

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


    @SneakyThrows
    public String createProject(Wait<WebDriver> wait, WebDriver driver, URL cytomineUrl,
                                String projectName) {
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
        webDriverUtils.byIsDisplayed(wait,
            By.xpath("//div[contains(text(), 'successfully deleted')]"));
    }

    public void listProjects(Wait<WebDriver> wait, URL cytomineUrl, Set<String> projectNames) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.xpathClick(wait, "//a[@href='#/projects']");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'New project')]"));
        Set<Boolean> ignored = projectNames.stream()
            .map(name -> webDriverUtils.byIsDisplayed(wait, By.xpath(format("//a[contains(text(), '%s')]", name))))
            .collect(toSet());
    }

    public String createOntology(Wait<WebDriver> wait, WebDriver driver, URL cytomineUrl,
                                 String ontologyName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.xpathClick(wait, "//a[@href='#/ontology']");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'New ontology')]");
        webDriverUtils.bySendKeys(wait, By.name("name"), ontologyName);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath(
            "//p[contains(@class, 'panel-heading') and contains(text(), '" + ontologyName + "')]"));
        return driver.getCurrentUrl();
    }

    public void deleteOntology(Wait<WebDriver> wait, String ontologyURL) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Delete')]");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.byIsDisplayed(wait,
            By.xpath("//div[contains(text(), 'successfully deleted')]"));
    }

    public String getOntologyUrlFromProject(Wait<WebDriver> wait, String projectURL) {
        webDriverUtils.goTo(wait, projectURL);
        webDriverUtils.xpathClick(wait, "//a[contains(@href, '/information')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//td[contains(text(), 'Ontology')]"));
        return wait.until(d -> {
            var elements = d.findElements(By.xpath("//a[contains(@href, '/ontology/')]"));
            return elements.get(0).getAttribute("href");
        });
    }

    @SneakyThrows
    public String addImage(Wait<WebDriver> wait, URL cytomineUrl,
                           Optional<String> maybeProjectName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/storage");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'Add files')]"));

        String imageName = "selenium-" + UUID.randomUUID() + ".png";
        Path tempDir = Files.createTempDirectory("selenium-upload");
        Path copiedFile = tempDir.resolve(imageName);
        try (var in = getClass().getClassLoader().getResourceAsStream("cat.png")) {
            Files.copy(in, copiedFile);
        }
        maybeProjectName.ifPresent(projectName -> selectProject(wait, projectName));

        webDriverUtils.bySendKeysWait(wait, By.cssSelector("input[type='file']"),
            copiedFile.toString(), false);
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'Start upload') and not(@disabled)]"));
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Start upload')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath(
            "//div[contains(@class,'uploaded-files-list')]//*[contains(text(),'" + imageName
                + "')]"));
        webDriverUtils.byIsDisplayed(wait, By.xpath(
            "//div[contains(@class,'uploaded-files-list')]//span[@data-status='success']"));
        return imageName;
    }

    private void selectProject(Wait<WebDriver> wait, String projectName) {
        webDriverUtils.byClick(wait, By.cssSelector(".project-select .multiselect__tags"));
        webDriverUtils.xpathClick(wait, "//span[@data-option='" + projectName + "']");
    }

    @SneakyThrows
    public void deleteImage(Wait<WebDriver> wait, URL cytomineUrl,
                            String imageName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/storage");
        Thread.sleep(5000);
        webDriverUtils.waitLoading(wait);
        webDriverUtils.byIsDisplayed(wait, By.xpath(
            "//div[contains(@class,'uploaded-files-list')]//span[@data-filename='" + imageName
                + "']"));
        webDriverUtils.byClick(wait, By.xpath(
            "//div[contains(@class,'uploaded-files-list')]//button[@data-filename='" + imageName
                + "']"));
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.waitUntilByEmpty(wait, By.xpath(
            "//div[contains(@class,'uploaded-files-list')]//span[@data-filename='" + imageName
                + "']"));
    }

    public String addTermToOntology(Wait<WebDriver> wait, WebDriver driver, String ontologyURL,
                                    String termName) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Add a term')]");
        webDriverUtils.bySendKeys(wait, By.name("name"), termName);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Save')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath(
            "//span[contains(@class, 'ontology-term') and contains(text(), '" + termName + "')]"));
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

    @SneakyThrows
    public void openImageInViewer(Wait<WebDriver> wait, WebDriver driver, String projectURL) {
        webDriverUtils.goTo(wait, projectURL);
        webDriverUtils.xpathClick(wait, "//li//a[.//i[contains(@class, 'fa-image')]]");
        Thread.sleep(2000);
        webDriverUtils.waitLoading(wait);
        webDriverUtils.byIsDisplayed(wait, By.xpath("//td//a[contains(@href, '/image/')]"));
        webDriverUtils.xpathClick(wait, "//td//a[contains(@href, '/image/')]");
        Thread.sleep(1000);
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

    @SneakyThrows
    public void drawRectangle(Wait<WebDriver> wait, WebDriver driver, int xOffset, int yOffset) {
        WebElement mapCanvas = webDriverUtils.waitForCanvasReady(wait, By.cssSelector(".ol-viewport canvas"));

        int canvasWidth = mapCanvas.getSize().getWidth();
        int canvasHeight = mapCanvas.getSize().getHeight();
        int startX = canvasWidth / 4;
        int startY = canvasHeight / 4;
        int endX = canvasWidth * 3 / 4;
        int endY = canvasHeight * 3 / 4;

        Actions actions = new Actions(driver);
        actions.moveToElement(mapCanvas, startX - canvasWidth / xOffset, startY - canvasHeight / yOffset)
            .click()
            .moveToElement(mapCanvas, endX - canvasWidth / 2, endY - canvasHeight / 2)
            .click()
            .perform();

        Thread.sleep(2000);
    }

    public void drawRectangle(Wait<WebDriver> wait, WebDriver driver) {
        drawRectangle(wait, driver, DEFAULT_CANVA_OFFSET, DEFAULT_CANVA_OFFSET);
    }

    public void drawRectangleAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        webDriverUtils.xpathClick(wait, "//button[.//i[contains(@class, 'fa-square')]]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//button[contains(@class, 'is-selected') and .//i[contains(@class, 'fa-square')]]")
        );

        drawRectangle(wait, driver);
    }

    public void drawRandomRectangleAnnotation(Wait<WebDriver> wait, WebDriver driver) {
        webDriverUtils.xpathClick(wait, "//button[.//i[contains(@class, 'fa-square')]]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//button[contains(@class, 'is-selected') and .//i[contains(@class, 'fa-square')]]")
        );

        int xOffset = new Random().nextInt(3) + DEFAULT_CANVA_OFFSET;
        int yOffset = new Random().nextInt(3) + DEFAULT_CANVA_OFFSET;
        drawRectangle(wait, driver, xOffset, yOffset);
    }

    public void drawRectangleAnnotationWithMagicWand(Wait<WebDriver> wait, WebDriver driver) {
        webDriverUtils.xpathClick(wait, "//button[.//i[contains(@class, 'fa-magic')]]");
        webDriverUtils.byIsDisplayed(
            wait,
            By.xpath("//button[contains(@class, 'is-selected') and .//i[contains(@class, 'fa-magic')]]")
        );

        drawRectangle(wait, driver);
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
        drawRectangleAnnotation(wait, driver);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Search for similar annotations')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//*[contains(text(), 'Similar annotations')]"));
        wait.until(d -> d.findElements(By.cssSelector(".similar-annotations-playground .annotation-data")).size() == nbAnnotations);
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
        webDriverUtils.byIsDisplayed(wait,
            By.xpath("//p[contains(@class, 'title') and contains(text(), '" + taskName + "')]"));
        webDriverUtils.xpathClick(wait,
            "//div[contains(@class, 'card') and .//p[contains(@class, 'title') and contains(text(), '" + taskName
                + "')]]//a[contains(text(), 'More')]"
        );

        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".panel-heading .panel-actions"));
        webDriverUtils.byClick(wait, By.cssSelector(".panel-actions .dropdown .icon"));

        webDriverUtils.xpathClick(wait,
            "//a[contains(@class, 'dropdown-item') and .//span[contains(text(), 'Delete')]]");

        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");

        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(text(), 'App deleted successfully')]"));
    }

    public void selectTask(Wait<WebDriver> wait, String taskName, String taskVersion) {
        webDriverUtils.byClick(wait, By.cssSelector(".executor .select select"));

        String optionXpath = format(
            "//div[contains(@class, 'executor')]//option[contains(text(), '%s') and contains(text(), '%s')]",
            taskName, taskVersion
        );
        webDriverUtils.xpathClick(wait, optionXpath);
        webDriverUtils.byIsDisplayed(wait, By.xpath(format("//div[contains(@class, 'executor')]//p[contains(text(), '%s') and contains(text(), '%s')]", taskName, taskVersion)));
        webDriverUtils.byIsDisplayed(wait, By.xpath("//section[contains(@class, 'fields')]//button[.//span[contains(text(), 'Select')]]"));    }

    public void selectAnnotationForGeometryInput(Wait<WebDriver> wait) {
        webDriverUtils.xpathClick(wait, "//section[contains(@class, 'fields')]//button[.//span[contains(text(), 'Select')]]");
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".modal-card .annotation-content"));
        wait.until(d -> !d.findElements(By.cssSelector(".annotation-content > div")).isEmpty());

        webDriverUtils.byClick(wait, By.cssSelector(".annotation-content > div:first-child"));
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".annotation-content > div.selected"));

        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".modal-card .modal-card-foot .is-link"));
        webDriverUtils.byClick(wait, By.cssSelector(".modal-card .modal-card-foot .is-link"));

        webDriverUtils.byIsDisplayed(wait, By.xpath("//div[contains(@class, 'annotation-container') and contains(text(), 'Annotation')]"));
    }

    public void runTask(Wait<WebDriver> wait, WebDriver driver) {
        webDriverUtils.byIsDisplayed(wait, By.cssSelector(".executor .card-content section:last-child .button.is-primary:last-child"));
        webDriverUtils.byClick(wait, By.cssSelector(".executor .card-content section:last-child .button.is-primary:last-child"));

        Wait<WebDriver> longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        webDriverUtils.byIsDisplayed(longWait, By.cssSelector(".runs .fa-check-circle"));
    }
}
