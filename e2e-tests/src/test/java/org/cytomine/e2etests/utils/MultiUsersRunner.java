package org.cytomine.e2etests.utils;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.cytomine.e2etests.api.KeycloakClient;
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

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Value("${cytomine.admin.username}")
    String adminUsername;

    @Value("${cytomine.admin.password}")
    String adminPassword;

    public void runAsAdmin(Wait<WebDriver> wait, WebDriver driver, Consumer<TestData> test) {
        run(wait, driver, List.of(ROLE_ADMIN), test);
    }

    public void runAllRoles(Wait<WebDriver> wait, WebDriver driver, Consumer<TestData> test) {
        run(wait, driver, List.of(ROLE_GUEST, ROLE_ADMIN, ROLE_USER), test);
    }

    public void run(Wait<WebDriver> wait, WebDriver driver, List<Role> roles, Consumer<TestData> test) {
        for (Role role : roles) {
            Wait<WebDriver> adminWait = new WebDriverWait(driver, Duration.ofSeconds(60));
            cytomineSteps.login(adminWait, cytomineUrl, adminUsername, adminPassword);
            String username =
                "selenium-" + role.name().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);
            String password = "Selenium1!";
            CreatedUser user = createUser(adminWait, role, username, password);
            cytomineSteps.logout(adminWait, cytomineUrl);
            try {
                String projectName = "selenium-project-" + UUID.randomUUID().toString().substring(0, 8);
                cytomineSteps.login(wait, cytomineUrl, user.username(), user.password());
                String projectUrl = cytomineSteps.createProject(wait, driver, cytomineUrl, projectName);
                String ontologyUrl = cytomineSteps.getOntologyUrlFromProject(wait, projectUrl);

                try {
                    test.accept(new TestData(role,
                        username,
                        password,
                        projectName,
                        projectUrl, projectName, ontologyUrl
                    ));
                } finally {
                    cytomineSteps.deleteProject(wait, projectUrl);
                    cytomineSteps.deleteOntology(wait, ontologyUrl);
                    cytomineSteps.logout(wait, cytomineUrl);
                }
            } finally {
                keycloakClient.deleteUser(user.username());
            }
        }
    }

    private CreatedUser createUser(Wait<WebDriver> wait, Role role, String username, String password) {
        cytomineSteps.createUser(wait, cytomineUrl, username, "Selenium", username, username + "@selenium.test",
            password, label(role));
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
