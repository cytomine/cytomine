package be.cytomine.controller.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import be.cytomine.common.repository.http.AnnotationHttpContract;
import be.cytomine.common.repository.http.AnnotationTermHttpContract;
import be.cytomine.common.repository.http.HealthService;
import be.cytomine.common.repository.http.ReviewedAnnotationHttpContract;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.http.UserAnnotationHttpContract;


@Configuration
public class RepositoryClient {

    @Value("${application.repositoryURL}")
    private String repositoryURL;

    @Bean
    RestClient repositoryRestClient() {
        return RestClient.builder().baseUrl(repositoryURL).build();
    }

    @Bean
    AnnotationHttpContract annotationServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AnnotationHttpContract.class);
    }

    @Bean
    HealthService healthServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(HealthService.class);
    }

    @Bean
    TermHttpContract termServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(TermHttpContract.class);
    }

    @Bean
    TermRelationHttpContract termRelationServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(TermRelationHttpContract.class);
    }

    @Bean
    AnnotationTermHttpContract annotationTermServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AnnotationTermHttpContract.class);
    }

    @Bean
    UserAnnotationHttpContract userAnnotationServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(UserAnnotationHttpContract.class);
    }

    @Bean
    ReviewedAnnotationHttpContract reviewedAnnotationServiceClient(RestClient repositoryRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ReviewedAnnotationHttpContract.class);
    }
}
