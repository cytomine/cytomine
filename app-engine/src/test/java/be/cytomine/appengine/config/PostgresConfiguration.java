package be.cytomine.appengine.config;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class PostgresConfiguration {
    @Container
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:14");
}
