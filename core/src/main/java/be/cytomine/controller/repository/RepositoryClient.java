package be.cytomine.controller.repository;

import org.cytomine.common.repository.http.HealthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RepositoryClient {

    @Bean
    HealthService healthServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
            .build();
        return factory.createClient(HealthService.class);
    }
}
