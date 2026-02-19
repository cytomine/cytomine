package org.cytomine.e2etests.ui;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class CytomineSteps {

    public void login(WebDriver driver, URL cytomineUrl, String username, String password) {
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(d ->
        {
            d.get(cytomineUrl.toString());
            d.findElement(By.id("username"))
                    .isDisplayed();
            d.findElement(By.id("username"))
                    .sendKeys(username);
            d.findElement(By.id("password"))
                    .sendKeys(password);
            d.findElement(By.cssSelector("input[type='submit']"))
                    .click();
            assertTrue(d.getTitle()
                    .equals("Cytomine"));
            d.findElement(By.id("app"))
                    .isDisplayed();
            return true;
        });
    }

    @SneakyThrows
    public URL createProject(WebDriver driver, URL cytomineUrl, String projectName) {
        driver.get(cytomineUrl.toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        wait.until(d -> driver.findElement(By.id("view-all-projects"))
                .isDisplayed());
        driver.findElement(By.id("view-all-projects"))
                .click();
        wait.until(d -> driver.findElement(By.id("new-project"))
                .isDisplayed());
        driver.findElement(By.id("new-project"))
                .click();
        driver.findElement(By.name("name"))
                .sendKeys(projectName);
        driver.findElement(By.id("button-save"))
                .click();
        wait.until(d -> driver.findElement(By.id("project-name-left-panel")));
        return URI.create(driver.getCurrentUrl())
                .toURL();
    }

    public void deleteProject(WebDriver driver, URL projectURL) {

    }

}
