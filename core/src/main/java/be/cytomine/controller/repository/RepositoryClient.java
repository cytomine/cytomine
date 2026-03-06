package be.cytomine.controller.repository;

import org.cytomine.common.repository.http.HealthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(types = {HealthService.class,})
public class RepositoryClient {
    @Bean
    RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> groups.forEachClient(
            (group, builder) -> builder.baseUrl("https://jsonplaceholder.typicode.com/").build());
    }
}
