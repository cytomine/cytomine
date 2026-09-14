package org.cytomine.e2etests.annotations;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

import be.cytomine.common.repository.model.Role;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.cytomine.e2etests.api.KeycloakClient;
import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Aspect
@Configurable
public class WithUserRolesAspect {

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

    @Around("@annotation(withUserRoles)")
    public Object aroundTestWithUserRoles(
        ProceedingJoinPoint joinPoint,
        WithUserRoles withUserRoles
    ) throws Throwable {
        WebDriver adminDriver = seleniumDriver.driver();
        Wait<WebDriver> adminWait = new WebDriverWait(adminDriver, Duration.ofSeconds(60));
        Object result = null;
        try {
            cytomineSteps.login(adminWait, cytomineUrl, adminUsername, adminPassword);
            for (Role role : withUserRoles.userRoles()) {
                CreatedUser user = createUser(adminWait, role);
                try {
                    result = proceedWithUser(joinPoint, user);
                } finally {
                    keycloakClient.deleteUser(user.username());
                }
            }
            cytomineSteps.logout(adminWait, cytomineUrl);
        } finally {
            adminDriver.quit();
        }
        return result;
    }

    private Object proceedWithUser(ProceedingJoinPoint joinPoint, CreatedUser user) throws Throwable {
        Object[] args = joinPoint.getArgs();
        args[args.length - 1] = user;
        return joinPoint.proceed(args);
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
