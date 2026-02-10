package be.cytomine.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class MongoTestConfiguration {

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;

    @Bean
    public MongoDBContainer mongoDBContainer() {
        DockerImageName imageName = DockerImageName.parse("mongo:4.4-focal");
        MongoDBContainer mongoContainer = new MongoDBContainer(imageName);
        mongoContainer.start();
        return mongoContainer;
    }

    @Bean
    public MongoClient mongoClient(MongoDBContainer mongoDBContainer) {
        return MongoClients.create(mongoDBContainer.getReplicaSetUrl());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDBContainer mongoDBContainer) {
        String uri = mongoDBContainer.getReplicaSetUrl();
        return new MongoTemplate(MongoClients.create(uri), mongoDatabaseName);
    }
}
