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

    public String deleteOntology(Wait<WebDriver> wait, String ontologyURL) {
        webDriverUtils.goTo(wait, ontologyURL);
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Delete')]");
        webDriverUtils.xpathClick(wait, "//button[contains(text(), 'Confirm')]");
        webDriverUtils.byIsDisplayed(wait,
            By.xpath("//div[contains(text(), 'successfully deleted')]"));
        return ontologyURL;
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
}
