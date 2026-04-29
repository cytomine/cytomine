package org.cytomine.e2etests.configuration;

import java.net.URL;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeleniumDriver {

    public static final String DOWNLOAD_PATH = "/tmp/selenium/";

    @Value("${selenium.url}")
    Optional<URL> seleniumUrl;

    public WebDriver driver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.dir", DOWNLOAD_PATH);
        options.addPreference("browser.download.useDownloadDir", true);
        options.addPreference(
            "browser.helperApps.neverAsk.saveToDisk",
            "application/pdf,application/octet-stream,text/csv,application/zip"
        );
        options.addPreference("browser.download.manager.showWhenStarting", false);
        options.addPreference("pdfjs.disabled", true);

        WebDriver webDriver = seleniumUrl.map(url -> {
            log.info("Instantiated RemoteWebDriver with url: {}", url);
            options.addArguments("--headless");
            RemoteWebDriver driver = (RemoteWebDriver) RemoteWebDriver.builder().address(url).oneOf(options).build();
            driver.setFileDetector(new LocalFileDetector());
            return driver;
        }).orElseGet(() -> {
            log.info("Instantiated FirefoxDriver");
            return new FirefoxDriver(options);
        });
        // This does not work. I am leaving it here so that we know.
        // https://www.selenium.dev/documentation/webdriver/waits/ <- I could not make it work.
        //        webDriver.manage()
        //                .timeouts()
        //                .implicitlyWait(Duration.ofSeconds(10));
        return webDriver;
    }
}
