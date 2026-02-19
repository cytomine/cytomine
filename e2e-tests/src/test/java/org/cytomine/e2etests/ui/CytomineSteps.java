package org.cytomine.e2etests.ui;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class CytomineSteps {

    public void login(WebDriver d, URL cytomineUrl, String username, String password) {

        d.findElement(By.id("username"))
                .isDisplayed();
        d.findElement(By.id("username"))
                .sendKeys(username);
        d.findElement(By.id("password"))
                .sendKeys(password);
        d.findElement(By.cssSelector("input[type='submit']"))
                .click();
        assertEquals("Cytomine", d.getTitle());
        d.findElement(By.id("app"))
                .isDisplayed();

    }

    /**
     * @return the URL of the created project
     */
    @SneakyThrows
    public URL createProject(WebDriver driver, URL cytomineUrl, String projectName) {
        driver.get(cytomineUrl.toString());
        driver.findElement(By.id("view-all-projects"))
                .isDisplayed();
        driver.findElement(By.id("view-all-projects"))
                .click();
        driver.findElement(By.id("new-project"))
                .isDisplayed();
        driver.findElement(By.id("new-project"))
                .click();
        driver.findElement(By.name("name"))
                .sendKeys(projectName);
        driver.findElement(By.id("button-save"))
                .click();
        driver.findElement(By.id("project-name-left-panel"));
        return URI.create(requireNonNull(driver.getCurrentUrl()))
                .toURL();
    }

    public void deleteProject(WebDriver driver, URL projectURL) {

    }

}
