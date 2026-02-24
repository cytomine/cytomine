package org.cytomine.e2etests.ui;

import java.net.URL;
import java.util.Set;

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
        return projectURL;
    }

    public void listProjects(Wait<WebDriver> wait, URL cytomineUrl, Set<String> projectNames) {
        webDriverUtils.goTo(wait, cytomineUrl.toString());
        webDriverUtils.xpathClick(wait, "//a[@href='#/projects']");
        Set<Boolean> ignored = projectNames.stream()
                .map(name ->
                        webDriverUtils.byIsDisplayed(wait,
                                By.xpath(format("//a[contains(text(), '%s')]",
                                        name))))
                .collect(toSet());

    }

}
