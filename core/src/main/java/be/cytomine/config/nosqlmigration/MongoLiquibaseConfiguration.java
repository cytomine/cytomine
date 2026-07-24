package be.cytomine.config.nosqlmigration;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import jakarta.annotation.PostConstruct;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.ext.mongodb.database.MongoConnection;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MongoLiquibaseConfiguration {

    private static final String CHANGELOG = "config/mongo/changelog/master.xml";

    private final MongoClient mongoClient;

    private final String databaseName;

    public MongoLiquibaseConfiguration(
        MongoClient mongoClient,
        @Value("${spring.data.mongodb.database}") String databaseName
    ) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    @PostConstruct
    public void update() throws Exception {
        log.info("Running MongoDB Liquibase changelog");

        MongoLiquibaseDatabase database = new MongoLiquibaseDatabase();
        MongoConnection connection = new MongoConnection();
        connection.setMongoClient(mongoClient);
        connection.setMongoDatabase(mongoClient.getDatabase(databaseName));
        connection.setConnectionString(new ConnectionString("mongodb://localhost/" + databaseName));
        database.setConnection(connection);

        // The MongoClient is a shared, Spring-managed bean: run the changelog but do not close
        // the Liquibase instance, as closing it would close the underlying client.
        Liquibase liquibase = new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }
}
