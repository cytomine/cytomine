package org.cytomine.e2etests.utils;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.cytomine.e2etests.api.KeycloakClient;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.Role;

import static be.cytomine.common.repository.model.Role.ROLE_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_GUEST;
import static be.cytomine.common.repository.model.Role.ROLE_USER;

@Component
public class MultiUsersRunner {

    @Autowired
    CytomineSteps cytomineSteps;

    @Autowired
    KeycloakClient keycloakClient;

    @Autowired
    SeleniumDriver seleniumDriver;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    public void runAsAdmin(Wait<WebDriver> wait, Consumer<CreatedUser> test) {
        run(wait, List.of(ROLE_ADMIN), test);
    }

    public void runAllRoles(Wait<WebDriver> wait, Consumer<CreatedUser> test) {
        run(wait, List.of(ROLE_GUEST, ROLE_ADMIN, ROLE_USER), test);
    }

    public void run(Wait<WebDriver> wait, List<Role> roles, Consumer<CreatedUser> test) {
        WebDriver adminDriver = seleniumDriver.driver();
        Wait<WebDriver> adminWait = new WebDriverWait(adminDriver, Duration.ofSeconds(60));
        try {
            cytomineSteps.login(adminWait, cytomineUrl, adminUsername, adminPassword);
            for (Role role : roles) {
                CreatedUser user = createUser(adminWait, role);
                try {
                    cytomineSteps.login(wait, cytomineUrl, user.username(), user.password());
                    test.accept(user);
                    cytomineSteps.logout(wait, cytomineUrl);
                } finally {
                    keycloakClient.deleteUser(user.username());
                }
            }
            cytomineSteps.logout(adminWait, cytomineUrl);
        } finally {
            adminDriver.quit();
        }
    }

    private CreatedUser createUser(Wait<WebDriver> wait, Role role) {
        String username = "selenium-" + role.name().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String password = "Selenium1!";
        cytomineSteps.createUser(
            wait, cytomineUrl, username, "Selenium", role.name(), username + "@selenium.test", password, label(role)
        );
        return new CreatedUser(role, username, password);
    }

    private String label(Role role) {
        return switch (role) {
            case ROLE_GUEST -> "Guest";
            case ROLE_USER -> "User";
            case ROLE_ADMIN -> "Admin";
            case ROLE_SUPER_ADMIN -> throw new IllegalArgumentException(
                "ROLE_SUPER_ADMIN cannot be assigned through the admin user creation form");
        };
    }
}
