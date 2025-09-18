package be.cytomine.appengine.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class HasPostgres {
    @Bean
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer("postgres:14")
            .withUsername("appengine")
            .withDatabaseName("appengine")
            .withPassword("password");
    }
}
