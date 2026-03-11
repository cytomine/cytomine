package org.cytomine.common;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = LiquibaseTestApplication.class)
@Import(PostGisTestConfiguration.class)
class LiquibaseIntegrationTest {

    @Autowired
    private PostgreSQLContainer postgres;

    @Test
    void liquibaseChangelogCanBeExecuted() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try (Liquibase liquibase = new Liquibase(
                    "config/liquibase/master.xml",
                    new ClassLoaderResourceAccessor(),
                    database)) {

                liquibase.update("");

                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT COUNT(*) FROM databasechangelog")) {
                    assertTrue(rs.next());
                    int changesetCount = rs.getInt(1);
                    assertTrue(changesetCount > 0,
                            "Expected at least one changeset to be executed");
                }
            }
        }
    }
}
