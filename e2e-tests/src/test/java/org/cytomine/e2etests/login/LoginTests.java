package org.cytomine.e2etests.login;

import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootTest
@Import({SeleniumDriver.class, CytomineSteps.class})
public class LoginTests {
    @Autowired
    SeleniumDriver driverProvider;

    WebDriver driver;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @Autowired
    CytomineSteps cytomineSteps;

    @BeforeEach
    void setUp(){
         driver = driverProvider.driver();
    }

    @AfterEach
    void tearDown(){
        driver.close();
    }

    @Test
    void login() {
        cytomineSteps.login(driver, cytomineUrl, adminUsername, adminPassword);
    }
}
