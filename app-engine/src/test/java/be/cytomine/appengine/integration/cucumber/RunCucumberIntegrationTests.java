package be.cytomine.appengine.integration.cucumber;

import java.io.IOException;

import com.cytomine.registry.client.RegistryClient;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {"pretty", "html:build/reports/tests/cucumber/cucumber-report.html"},
    features = {"src/test/resources"},
    glue = {"be.cytomine.appengine.integration.cucumber"}
    //,tags = "not @Scheduler"
)
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RunCucumberIntegrationTests {

    public static final int REGISTRY_INTERNAL_PORT = 5000;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Container
    static GenericContainer<?> registryContainer = new GenericContainer<>("registry:2.8.3")
            .withExposedPorts(REGISTRY_INTERNAL_PORT);

    @BeforeClass
    public static void startContainers() throws IOException {
        registryContainer.start();

        String registryUrl = String.format(
                "http://%s:%d",
                registryContainer.getHost(),
                registryContainer.getMappedPort(REGISTRY_INTERNAL_PORT)
        );

        System.setProperty("registry.url", registryUrl);

        RegistryClient.config(registryUrl);
    }
}
