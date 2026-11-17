package org.cytomine.e2etests.selenium;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import org.cytomine.e2etests.configuration.SeleniumDriver;
import org.cytomine.e2etests.ui.CytomineSteps;
import org.cytomine.e2etests.ui.WebDriverUtils;
import org.cytomine.e2etests.utils.MultiUsersRunner;
import org.cytomine.e2etests.utils.Screenshots;
import org.cytomine.e2etests.utils.TestImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static java.util.UUID.randomUUID;

@Import({CytomineSteps.class, MultiUsersRunner.class, SeleniumDriver.class, WebDriverUtils.class})
@SpringBootTest
public class ImageFormatTests {
    @Autowired
    SeleniumDriver driverProvider;
    WebDriver driver;
    Wait<WebDriver> wait;

    @Value("${cytomine.url}")
    URL cytomineUrl;

    @Autowired
    CytomineSteps cytomineSteps;

    @Autowired
    MultiUsersRunner multiUsers;

    @BeforeEach
    void setUp() {
        driver = driverProvider.driver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        Screenshots.save(
            driver,
            "closing-" + testInfo.getTestMethod().map(Method::getName).orElseGet(() -> "no-name-" + randomUUID())
        );
        driver.quit();
    }

    private void cleanup(Runnable... actions) {
        for (Runnable action : actions) {
            try {
                action.run();
            } catch (Exception e) {
                // best-effort cleanup; the original test failure must not be masked
            }
        }
    }

    private void uploadImageAndCheckFormat(String resourcePath, String format) {
        multiUsers.runAsAdmin(
            wait, driver, admin -> {
                String imageName = "selenium-" + randomUUID() + "." + format;

                cytomineSteps.addImageFromResource(
                    wait,
                    driver,
                    cytomineUrl,
                    resourcePath,
                    imageName,
                    Optional.of(admin.projectName())
                );
                try {
                    cytomineSteps.verifyImageThumbnail(wait, admin.projectUrl(), imageName);
                    cytomineSteps.openImageInViewer(wait, admin.projectUrl());
                    cytomineSteps.verifyImageViewerCanvas(wait);
                } finally {
                    cleanup(() -> cytomineSteps.deleteImage(wait, cytomineUrl, imageName));
                }
            }
        );
    }

    @Test
    void uploadImageAndCheckFormatSVS() {
        uploadImageAndCheckFormat(TestImage.SVS.getResourcePath(), TestImage.SVS.getExtension());
    }
}
