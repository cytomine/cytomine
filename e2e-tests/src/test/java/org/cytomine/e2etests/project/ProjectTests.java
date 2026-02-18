package org.cytomine.e2etests.project;

import java.net.URL;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class ProjectTests {
    @Autowired
    WebDriver driver;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @Test
    void createProject() {
        driver.get(cytomineUrl.toString());
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(d -> driver.findElement(By.id("username")).isDisplayed());
        driver.findElement(By.id("username")).sendKeys(adminUsername);
        driver.findElement(By.id("password")).sendKeys(adminPassword);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        wait.until(d -> driver.getTitle().equals("Cytomine"));
        wait.until(d -> driver.findElement(By.id("app")));
    }

}
