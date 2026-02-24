package org.cytomine.e2etests.project;

import static java.util.stream.Collectors.toSet;

import java.net.URL;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.cytomine.e2etests.ui.WebDriverUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import({SeleniumDriver.class, CytomineSteps.class, WebDriverUtils.class})
@SpringBootTest
public class CytomineTests {
  @Autowired SeleniumDriver driverProvider;
  WebDriver driver;
  Wait<WebDriver> wait;

  @Value("${cytomine.url}")
  URL cytomineUrl;

  @Value("${cytomine.admin.username}")
  String adminUsername;

  @Value("${cytomine.admin.password}")
  String adminPassword;

  @Autowired CytomineSteps cytomineSteps;

  @BeforeEach
  void setUp() {
    driver = driverProvider.driver();
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  @AfterEach
  void tearDown() {
    driver.close();
  }

  @Test
  void login() {
    cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
  }

  @Test
  void createProject() {
    String projectName = "selenium-" + UUID.randomUUID();
    cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
    String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
    cytomineSteps.deleteProject(wait, projectURL);
  }

  @Test
  void deleteProject() {
    String projectName = "selenium-" + UUID.randomUUID();
    cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
    String projectURL = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
    cytomineSteps.deleteProject(wait, projectURL);
  }

  @Test
  void listProjects() {
    Set<String> projectNames =
        Set.of(
            "selenium-" + UUID.randomUUID(),
            "selenium-" + UUID.randomUUID(),
            "selenium-" + UUID.randomUUID());
    cytomineSteps.login(wait, cytomineUrl, adminUsername, adminPassword);
    Set<String> projectUrls =
        projectNames.stream()
            .map(name -> cytomineSteps.createProject(wait, driver, cytomineUrl, name))
            .collect(toSet());
    cytomineSteps.listProjects(wait, cytomineUrl, projectNames);
    Set<String> ignored =
        projectUrls.stream()
            .map(projectUrl -> cytomineSteps.deleteProject(wait, projectUrl))
            .collect(toSet());
  }
}
