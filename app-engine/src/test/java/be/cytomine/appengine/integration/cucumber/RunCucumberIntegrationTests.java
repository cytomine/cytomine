package be.cytomine.appengine.integration.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import be.cytomine.appengine.config.K3sConfiguration;
import be.cytomine.appengine.config.PostgresConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty",
    "html:build/reports/tests/cucumber/cucumber-report.html"}, features = {
    "src/test/resources"}, glue = {"be.cytomine.appengine.integration.cucumber"}
    //,tags = "not @Scheduler"
)
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.datasource.url=jdbc:tc:postgresql:14:///appengine"})
@Import({K3sConfiguration.class, PostgresConfiguration.class})
@Disabled
public class RunCucumberIntegrationTests {
}
