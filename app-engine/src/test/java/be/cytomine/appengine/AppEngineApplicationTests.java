package be.cytomine.appengine;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:14:///appengine"
    }
)
class AppEngineApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine").withPassword("appengine").withUsername(
            "appengine").withConnectTimeoutSeconds(60);

    @Container
    @ServiceConnection
    static K3sContainer k3s =
        new K3sContainer(DockerImageName.parse("rancher/k3s:v1.30.14-rc3-k3s3"));

    @Test
     void contextLoads() {
    }
}
