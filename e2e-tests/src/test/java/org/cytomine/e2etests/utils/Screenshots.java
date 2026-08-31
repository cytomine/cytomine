package org.cytomine.e2etests.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import lombok.SneakyThrows;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static org.openqa.selenium.OutputType.FILE;

public final class Screenshots {

    @SneakyThrows
    public static void save(WebDriver driver, String name) {
        Path destination = Paths.get("./build/reports/" + name + ".jpg");
        Files.createDirectories(Path.of("./build/reports/"));
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(FILE);
        Files.move(screenshot.toPath(), destination, REPLACE_EXISTING);
        Files.setPosixFilePermissions(destination, Set.of(OTHERS_READ, OWNER_READ, GROUP_READ));
    }
}
