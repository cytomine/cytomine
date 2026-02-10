package org.cytomine.e2etests.login;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoginTests {
    WebDriver driver = new FirefoxDriver();

    @Value("${cytomine.url}")
    String cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    @Test
    void login() {
        driver.get(cytomineUrl);
    }
}
