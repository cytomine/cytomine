package be.cytomine.appengine.config;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public interface HasPostgres {
    @Container
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:14")
                                                  .withUsername("appengine2")
                                                  .withDatabaseName("appengine2")
                                                  .withPassword("password2");

}
