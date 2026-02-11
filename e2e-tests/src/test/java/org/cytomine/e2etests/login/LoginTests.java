package org.cytomine.e2etests.login;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoginTests {
    WebDriver driver;

    @Value("${cytomine.url}")
    String cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @BeforeEach
    void setUp() {
        driver = new FirefoxDriver();
    }

    @AfterEach
    void afterAll() {
        driver.close();
    }

    @Test
    void login() {
        driver.get(cytomineUrl);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(d -> driver.findElement(By.id("username")).isDisplayed());
        driver.findElement(By.id("username")).sendKeys(adminUsername);
        driver.findElement(By.id("password")).sendKeys(adminPassword);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        wait.until(d -> driver.getTitle().equals("Cytomine"));
        wait.until(d -> driver.findElement(By.id("app")));
    }
}
