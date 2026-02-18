package org.cytomine.e2etests.login;

import java.net.URL;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootTest
@Import(SeleniumDriver.class)
public class LoginTests {
    @Autowired
    WebDriver driver;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

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
