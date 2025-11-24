package be.cytomine.appengine;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import be.cytomine.appengine.config.K3sConfiguration;
import be.cytomine.appengine.config.PostgresConfiguration;

@SpringBootTest(
    properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:14:///appengine"
    }
)
@ActiveProfiles("test")
@Import({K3sConfiguration.class, PostgresConfiguration.class})
class AppEngineApplicationTests {

    @Test
    void contextLoads() {
    }
}
