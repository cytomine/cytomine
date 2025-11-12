package be.cytomine.appengine;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import be.cytomine.appengine.config.K3sConfiguration;

@SpringBootTest(
    properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:14:///appengine"
    }
)
@ActiveProfiles("test")
@Import(K3sConfiguration.class)
class AppEngineApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine").withPassword("appengine").withUsername(
            "appengine").withConnectTimeoutSeconds(60);

    @Test
    void contextLoads() {
    }
}
