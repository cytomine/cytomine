package be.cytomine.controller.repository;

import be.cytomine.common.repository.http.HealthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RepositoryClient {

    @Value("${application.repositoryURL}")
    private String repositoryURL;

    @Bean
    RestClient repositoryRestClient() {
        return RestClient.builder()
            .baseUrl(repositoryURL)
            .build();
    }

    @Bean
    HealthService healthServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
            .build();
        return factory.createClient(HealthService.class);
    }
}
