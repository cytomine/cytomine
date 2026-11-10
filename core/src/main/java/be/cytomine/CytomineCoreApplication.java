package be.cytomine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import be.cytomine.config.properties.ApplicationProperties;

@EnableMongoRepositories("be.cytomine.repositorynosql")
@EnableJpaRepositories("be.cytomine.repository")
@EntityScan("be.cytomine.domain")
@EnableConfigurationProperties(ApplicationProperties.class)
@SpringBootApplication(scanBasePackages = {"be.cytomine", "org.cytomine.common.mapper",
    "org.cytomine.common.config.security", "org.cytomine.common.keycloak", "org.cytomine.common.repository.utils",
    "org.cytomine.common.utils"})
public class CytomineCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(CytomineCoreApplication.class, args);
    }
}
