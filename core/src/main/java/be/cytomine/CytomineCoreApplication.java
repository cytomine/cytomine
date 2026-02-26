package be.cytomine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.gateway.mvc.config.ProxyResponseAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories("be.cytomine.repositorynosql")
@EnableJpaRepositories("be.cytomine.repository")
@EntityScan("be.cytomine.domain")
@SpringBootApplication(exclude = {ProxyResponseAutoConfiguration.class})
public class CytomineCoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(CytomineCoreApplication.class, args);
	}
}
