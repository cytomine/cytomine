package be.cytomine.appengine.config;

import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public interface HasPostgres {
    @Container
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:14")
        .withUsername("appengine")
        .withDatabaseName("appengine")
        .withPassword("password");

}
