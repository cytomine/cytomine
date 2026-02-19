package org.cytomine.e2etests.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Duration;

@Component
public class CytomineSteps {

    public void login(WebDriver driver, URL cytomineUrl, String username, String password) {
        driver.get(cytomineUrl.toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(d -> d.findElement(By.id("username"))
                .isDisplayed());
        driver.findElement(By.id("username"))
                .sendKeys(username);
        driver.findElement(By.id("password"))
                .sendKeys(password);
        driver.findElement(By.cssSelector("input[type='submit']"))
                .click();
        wait.until(d -> driver.getTitle()
                .equals("Cytomine"));
        wait.until(d -> driver.findElement(By.id("app")));
    }

    public String createProject(WebDriver driver, URL cytomineUrl, String projectName) {
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
        return driver.getCurrentUrl();
    }

    public void deleteProject(WebDriver driver, URL projectURL, String projectId) {
        driver.get(projectURL.toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        wait.until(d -> driver.findElement(By.id("view-all-projects"))
                .isDisplayed());
        driver.findElement(By.id("view-all-projects"))
                .click();
        wait.until(d -> driver.findElement(By.id("new-project"))
                .isDisplayed());
        driver.findElement(By.id("new-project"))
                .click();

        driver.findElement(By.id("button-save"))
                .click();
        wait.until(d -> driver.findElement(By.id("project-name-left-panel")));
    }

}
