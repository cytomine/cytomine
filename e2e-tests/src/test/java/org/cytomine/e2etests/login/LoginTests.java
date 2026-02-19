package org.cytomine.e2etests.login;

import lombok.extern.slf4j.Slf4j;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.net.URL;
import java.time.Duration;

@Slf4j
@SpringBootTest
@Import({SeleniumDriver.class, CytomineSteps.class})
public class LoginTests {
    @Autowired
    SeleniumDriver driverProvider;

    WebDriver driver;
    Wait<WebDriver> wait;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @Autowired
    CytomineSteps cytomineSteps;

    @BeforeEach
    void setUp() {
        driver = driverProvider.driver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    }

    @AfterEach
    void tearDown() {
        driver.close();
    }
    @Disabled
    @Test
    void login() {

            cytomineSteps.login(wait,driver, cytomineUrl, adminUsername, adminPassword);


    }
}
