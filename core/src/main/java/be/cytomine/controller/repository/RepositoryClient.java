package be.cytomine.controller.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import org.cytomine.common.beans.restclientfactory.RestClientFactory;
import org.cytomine.common.http.HealthService;
import org.cytomine.common.repository.http.CommandHttpContract;
import org.cytomine.common.repository.http.OntologyHttpContract;
import org.cytomine.common.repository.http.ProjectHttpContract;
import org.cytomine.common.repository.http.ReviewedAnnotationHttpContract;
import org.cytomine.common.repository.http.RoleHttpContract;
import org.cytomine.common.repository.http.StatsHttpContract;
import org.cytomine.common.repository.http.StorageHttpContract;
import org.cytomine.common.repository.http.TagDomainAssociationHttpContract;
import org.cytomine.common.repository.http.TagHttpContract;
import org.cytomine.common.repository.http.TermHttpContract;
import org.cytomine.common.repository.http.TermRelationHttpContract;
import org.cytomine.common.repository.http.UploadedFileHttpContract;
import org.cytomine.common.repository.http.UserHttpContract;
import org.cytomine.common.repository.http.UserRoleHttpContract;
import org.cytomine.common.repository.utils.SpringPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Configuration
public class RepositoryClient {

    @Value("${application.repositoryURL}")
    private String repositoryURL;

    private final RestClientFactory restClientFactory;

    // Not sure if it should or not be shared between each client instance.
    @Bean
    RestClient repositoryRestClient() {
        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Page.class, SpringPage.class);

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
            .modulesToInstall(module)
            .build();

        return RestClient.builder()
            .baseUrl(repositoryURL)
            .messageConverters(converters -> converters.addFirst(new MappingJackson2HttpMessageConverter(objectMapper)))
            .requestInterceptor(restClientFactory::forwardAuthorizationHeader)
            .build();
    }


    @Bean
    CommandHttpContract commandHttpContract(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, CommandHttpContract.class);
    }

    @Bean
    HealthService healthServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, HealthService.class);
    }

    @Bean
    OntologyHttpContract ontologyServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, OntologyHttpContract.class);
    }

    @Bean
    TermHttpContract termServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, TermHttpContract.class);
    }

    @Bean
    TermRelationHttpContract termRelationServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, TermRelationHttpContract.class);
    }

    @Bean
    StatsHttpContract statsServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, StatsHttpContract.class);
    }

    @Bean
    StorageHttpContract storageServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, StorageHttpContract.class);
    }

    @Bean
    RoleHttpContract roleServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, RoleHttpContract.class);
    }

    @Bean
    ReviewedAnnotationHttpContract reviewedAnnotationClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, ReviewedAnnotationHttpContract.class);
    }

    @Bean
    ProjectHttpContract projectHttpContract(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, ProjectHttpContract.class);
    }

    @Bean
    TagHttpContract tagHttpContract(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, TagHttpContract.class);
    }

    @Bean
    TagDomainAssociationHttpContract tagDomainAssociationClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, TagDomainAssociationHttpContract.class);
    }

    @Bean
    UploadedFileHttpContract uploadedFileServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, UploadedFileHttpContract.class);
    }

    @Bean
    UserRoleHttpContract userRoleServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, UserRoleHttpContract.class);
    }

    @Bean
    UserHttpContract userServiceClient(RestClient repositoryRestClient) {
        return restClientFactory.createClient(repositoryRestClient, UserHttpContract.class);
    }


}
