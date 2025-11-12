package be.cytomine.appengine.handlers.scheduler.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

//@Import(HasPostgres.class)
@SpringBootTest(
    properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:postgresql:14:///appengine"
    }
)
@RequiredArgsConstructor
@Testcontainers

class FailingConfigKubernetesSchedulerTest {
    @Container
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:14");

    @Autowired
    KubernetesScheduler kubernetesScheduler;

    @Test
    void init() {
    }
}
