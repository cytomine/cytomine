package org.cytomine.e2etests.configuration;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.net.http.HttpClient.Version.HTTP_1_1;

@Slf4j
@Component
public class SeleniumDriver {

    @Value("${selenium.url}")
    Optional<URL> seleniumUrl;

    public WebDriver driver() {
        FirefoxOptions options = new FirefoxOptions();
        WebDriver webDriver =
            seleniumUrl.map(url -> {
                    log.info("Instantiated RemoteWebDriver with url: {}", url);
                    options.addArguments("--headless");

                    // Configure HTTP client with proper timeouts for k3s environment
                    ClientConfig clientConfig = ClientConfig.defaultConfig()
                                                    .connectionTimeout(Duration.ofSeconds(30))
                                                    .readTimeout(Duration.ofMinutes(3))
                                                    .version(HTTP_1_1.toString());

                    return RemoteWebDriver.builder()
                               .address(url)
                               .oneOf(options)
                               .config(clientConfig)
                               .build();
                })
                .orElseGet(() ->
                    {
                        log.info("Instantiated FirefoxDriver");
                        return new FirefoxDriver(options);
                    }
                );
        // This does not work. I am leaving it here so that we know.
        // https://www.selenium.dev/documentation/webdriver/waits/ <- I could not make it work.
        //        webDriver.manage()
        //                .timeouts()
        //                .implicitlyWait(Duration.ofSeconds(10));
        return webDriver;
    }
}
