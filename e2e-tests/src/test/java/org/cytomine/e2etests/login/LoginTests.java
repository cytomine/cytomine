package org.cytomine.e2etests.login;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RequiredArgsConstructor
public class LoginTests {
    final WebDriver driver = new ChromeDriver();

    @Value("${cytomine.url}")
    final String cytomineUrl;

    @Value("${ADMIN_USERNAME}")
    final String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    final String adminPassword;

    @Test
    void login() {
        driver.get(cytomineUrl);
    }
}
