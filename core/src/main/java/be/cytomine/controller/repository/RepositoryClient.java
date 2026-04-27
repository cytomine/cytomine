package be.cytomine.controller.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import be.cytomine.common.repository.http.HealthService;
import be.cytomine.common.repository.http.ReviewedAnnotationHttpContract;
import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.http.TermRelationHttpContract;


@Configuration
public class RepositoryClient {

    @Value("${application.repositoryURL}")
    private String repositoryURL;

    // Not sure if it should or not be shared between each client instance.
    @Bean
    RestClient repositoryRestClient() {
        return RestClient.builder().baseUrl(repositoryURL).build();
    }

    @Bean
    HealthService healthServiceClient(RestClient repositoryRestClient) {
        return createClient(repositoryRestClient, HealthService.class);
    }

    @Bean
    TermHttpContract termServiceClient(RestClient repositoryRestClient) {
        return createClient(repositoryRestClient, TermHttpContract.class);
    }

    @Bean
    TermRelationHttpContract termRelationServiceClient(RestClient repositoryRestClient) {
        return createClient(repositoryRestClient, TermRelationHttpContract.class);
    }

    @Bean
    StatsHttpContract statsServiceClient(RestClient repositoryRestClient) {
        return createClient(repositoryRestClient, StatsHttpContract.class);
    }

    @Bean
    ReviewedAnnotationHttpContract reviewedAnnotationClient(RestClient repositoryRestClient) {
        return createClient(repositoryRestClient, ReviewedAnnotationHttpContract.class);
    }

    private <T> T createClient(RestClient repositoryRestClient, Class<T> repoType) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(repoType);
    }
}
