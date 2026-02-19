package org.cytomine.e2etests.project;

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

import java.net.URL;
import java.util.UUID;

@Import({SeleniumDriver.class,CytomineSteps.class})
@SpringBootTest
public class ProjectTests {
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
    void setUp() {
        driver = driverProvider.driver();
    }

    @AfterEach
    void tearDown() {
        driver.close();
    }

    @Test
    void createProject() {
        String projectName = "selenium-" + UUID.randomUUID();
        cytomineSteps.login(driver, cytomineUrl, adminUsername, adminPassword);
        cytomineSteps.createProject(driver, cytomineUrl, projectName);
    }

}
