package org.cytomine.e2etests.ui;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
public class CytomineSteps {

    public void login(Wait<WebDriver> wait, WebDriver driver, URL cytomineUrl, String username, String password) {
        driver.get(cytomineUrl.toString());
        wait.until(d -> {
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
            return true;
        });
    }

    /**
     * @return the URL of the created project
     */
    @SneakyThrows
    public URL createProject(Wait<WebDriver> wait, WebDriver driver, URL cytomineUrl, String projectName) {
        driver.get(cytomineUrl.toString());
        wait.until(d -> {
            d.findElement(By.xpath("//a[@href='#/projects']"))
                    .click();
            return true;
        });
        wait.until(d -> {

            d.findElement(By.xpath("//button[contains(text(), 'New project')]"))
                    .click();
            return true;
        });
        wait.until(d -> {
            d.findElement(By.name("name"))
                    .sendKeys(projectName);
            d.findElement(By.xpath("//button[contains(text(), 'Save')]"))
                    .click();
            return true;
        });
        wait.until(d -> {
            d.findElement(By.xpath("//h1[contains(text(), 'Project: ')]"))
                    .isDisplayed();
            return true;
        });

        return URI.create(requireNonNull(driver.getCurrentUrl()))
                .toURL();

    }

    public void deleteProject(WebDriver driver, URL projectURL) {

    }

}
