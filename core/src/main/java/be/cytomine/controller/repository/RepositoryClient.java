package be.cytomine.controller.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import be.cytomine.common.repository.http.HealthService;
import be.cytomine.common.repository.http.ReviewedAnnotationHttpContract;
import be.cytomine.common.repository.http.StatsHttpContract;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.utils.SpringPage;

@Configuration
public class RepositoryClient {

    @Value("${application.repositoryURL}")
    private String repositoryURL;

    // Not sure if it should or not be shared between each client instance.
    @Bean
    RestClient repositoryRestClient() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Page.class, SpringPage.class);
        objectMapper.registerModule(module);

        return RestClient.builder()
            .baseUrl(repositoryURL)
            .messageConverters(converters -> converters.addFirst(new MappingJackson2HttpMessageConverter(objectMapper)))
            .build();
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
