package org.cytomine.e2etests.configuration;

import java.net.URL;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeleniumDriver {

    @Value("${selenium.url}")
    Optional<URL> seleniumUrl;

    public WebDriver driver() {
        FirefoxOptions options = new FirefoxOptions();
        //options.addArguments("--headless");
        return
            seleniumUrl.map(url -> {
                    log.info("Instantiated RemoteWebDriver with url: {}", url);
                    return new RemoteWebDriver(url, options);
                })
                .orElseGet(() ->
                    {
                        log.info("Instantiated FirefoxDriver");
                        return new FirefoxDriver(options);
                    }
                );
    }
}
