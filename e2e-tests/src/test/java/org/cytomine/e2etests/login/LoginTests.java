package org.cytomine.e2etests.login;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoginTests {
    WebDriver driver;

    @Value("${selenium.url}")
    Optional<URL> seleniumUrl;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @BeforeEach
    void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver =
            seleniumUrl.map(url -> new RemoteWebDriver(url, options))
                .orElseGet(() -> new FirefoxDriver(options));
    }

    @AfterEach
    void afterAll() {
        driver.close();
    }

    @Test
    void login() {
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
