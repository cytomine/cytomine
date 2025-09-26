package be.cytomine.appengine.handlers.scheduler.impl;

import be.cytomine.appengine.config.HasPostgres;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(HasPostgres.class)
@SpringBootTest(
        properties = {
                "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
                "spring.datasource.url=jdbc:tc:postgresql:14:///integration-tests-db"}
)
@RequiredArgsConstructor
class FailingConfigKubernetesSchedulerTest {

    @Autowired
    KubernetesScheduler kubernetesScheduler;

    @Test
    void init() {
    }
}
