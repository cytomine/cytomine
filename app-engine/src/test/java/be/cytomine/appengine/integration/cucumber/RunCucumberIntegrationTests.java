package be.cytomine.appengine.integration.cucumber;

import java.io.IOException;

import com.cytomine.registry.client.RegistryClient;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {"pretty", "html:build/reports/tests/cucumber/cucumber-report.html"},
    features = {"src/test/resources"},
    glue = {"be.cytomine.appengine.integration.cucumber"}
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

    @Container
    static K3sContainer k3sContainer = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.21.3-k3s1"))
            .withCommand("server", "--disable", "metrics-server");

    @BeforeClass
    public static void startContainers() throws IOException {
        registryContainer.start();
        k3sContainer.start();

        String registryUrl = String.format(
                "http://%s:%d",
                registryContainer.getHost(),
                registryContainer.getMappedPort(REGISTRY_INTERNAL_PORT)
        );

        System.setProperty("registry.url", registryUrl);

        RegistryClient.config(registryUrl);

        String kubeConfigYaml = k3sContainer.getKubeConfigYaml();
        Config config = Config.fromKubeconfig(kubeConfigYaml);

        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, config.getMasterUrl());

        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            client.namespaces()
                    .resource(new NamespaceBuilder()
                            .withNewMetadata()
                            .withName("tasks")
                            .endMetadata()
                            .build())
                    .create();

            client.serviceAccounts()
                    .inNamespace("tasks")
                    .resource(new ServiceAccountBuilder()
                            .withNewMetadata()
                            .withName("app-engine")
                            .withNamespace("tasks")
                            .endMetadata()
                            .build())
                    .create();
        }
    }
}
