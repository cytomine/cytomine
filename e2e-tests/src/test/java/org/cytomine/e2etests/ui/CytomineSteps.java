package org.cytomine.e2etests.ui;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

@Component
public class CytomineSteps {

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

    /**
     * @return the URL of the created project
     */
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

    public String deleteProject(Wait<WebDriver> wait, String projectURL) {
        webDriverUtils.goTo(wait, projectURL);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Delete')]");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.byIsDisplayed(wait,
            By.xpath("//div[contains(text(), 'successfully deleted')]"));
        return projectURL;
    }

    public void listProjects(Wait<WebDriver> wait, URL cytomineUrl, Set<String> projectNames) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.xpathClick(wait, "//a[@href='#/projects']");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//button[contains(text(), 'New project')]"));
        Set<Boolean> ignored = projectNames.stream()
                                   .map(name ->
                                            webDriverUtils.byIsDisplayed(wait,
                                                By.xpath(format("//a[contains(text(), '%s')]",
                                                    name))))
                                   .collect(toSet());
    }

    @SneakyThrows
    public String addImage(Wait<WebDriver> wait, URL cytomineUrl,
                           Optional<String> maybeProjectName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/storage");
        String originalPath = getClass().getClassLoader().getResource("cat.png").getPath();
        String imageName = "selenium-" + UUID.randomUUID() + ".png";
        Path originalFile = Paths.get(originalPath);
        Path copiedFile = originalFile.resolveSibling(imageName);
        Files.copy(originalFile, copiedFile);
        maybeProjectName.ifPresent(projectName -> {
                webDriverUtils.xpathClick(wait,
                    "//strong[contains(text(), 'Link with project')]/ancestor::div[contains"
                        + "(@class, 'columns')]//div[contains(@class, 'multiselect__tags')]");
                webDriverUtils.xpathClick(wait,
                    "//strong[contains(text(), 'Link with project')]/ancestor::div[contains"
                        + "(@class, 'columns')]//li[contains(@class, 'multiselect__element') and "
                        + "not(contains(@class, 'multiselect__select-all'))]//span[contains(., '"
                        + projectName + "')]");
            }
        );

        webDriverUtils.bySendKeysWait(wait, By.cssSelector("input[type='file']"),
            copiedFile.toString(), false);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Start upload')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//td[contains(text(), '" + imageName + "')]"));
        return imageName;
    }

    @SneakyThrows
    public void deleteImage(Wait<WebDriver> wait, URL cytomineUrl,
                            String imageName) {
        webDriverUtils.goTo(wait, cytomineUrl.toString() + "/#/storage");
        webDriverUtils.xpathClick(wait, "//td[contains(text(), '" + imageName
                                            + "')]/ancestor::tr//button[contains(text(), "
                                            + "'Delete')]");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.byIsDisplayed(wait, By.xpath("//p[contains(text(), 'No uploaded file')]"));
    }

}
