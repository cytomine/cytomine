package org.cytomine.e2etests.configuration;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import jakarta.annotation.PreDestroy;
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

    @Value("${selenium.url}")
    Optional<URL> seleniumUrl;

    public WebDriver driver() {
        FirefoxOptions options = new FirefoxOptions();
        WebDriver webDriver = seleniumUrl.map(url -> {
                log.info("Instantiated RemoteWebDriver with url: {}", url);
                options.addArguments("--headless");
                RemoteWebDriver driver = (RemoteWebDriver) RemoteWebDriver.builder()
                    .address(url)
                    .oneOf(options)
                    .build();
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            })
            .orElseGet(() -> {
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

    @PreDestroy
    public void shutdownSeleniumServer() {
        seleniumUrl.ifPresent(url -> {
            try {
                log.info("Shutting down Selenium server at: {}", url);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/se/grid/node/drain"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Selenium shutdown response: {} {}", response.statusCode(), response.body());
            } catch (Exception e) {
                log.warn("Failed to shutdown Selenium server: {}", e.getMessage());
            }
        });
    }
}
