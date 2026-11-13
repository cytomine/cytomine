package org.cytomine.common.beans.authorizationclient;

import lombok.RequiredArgsConstructor;
import org.cytomine.common.authorizationapi.http.PermissionsHttpContract;
import org.cytomine.common.http.RestClientFactory;
import org.cytomine.common.repository.utils.SpringPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleAbstractTypeResolver;
import tools.jackson.databind.module.SimpleModule;

@RequiredArgsConstructor
@Component
public class AuthorizationRestClient {

    private final RestClientFactory restClientFactory;
    @Value("${application.authorizationApiURL}")
    private String authorizationApiURL;

    @Bean
    RestClient authorizationRestClient() {
        SimpleModule module = new SimpleModule();
        SimpleAbstractTypeResolver simpleAbstractTypeResolver = new SimpleAbstractTypeResolver();
        simpleAbstractTypeResolver.addMapping(Page.class, SpringPage.class);
        module.setAbstractTypes(simpleAbstractTypeResolver);

        JsonMapper objectMapper = JsonMapper.builder()
            .addModule(module)
            .build();

        return RestClient.builder()
            .baseUrl(authorizationApiURL).configureMessageConverters(
                converters -> converters.addCustomConverter(new JacksonJsonHttpMessageConverter(objectMapper)))
            .requestInterceptor(restClientFactory::forwardAuthorizationHeader)
            .build();
    }

    @Bean
    PermissionsHttpContract permissionsHttpContract(RestClient restClient) {
        return restClientFactory.createClient(restClient, PermissionsHttpContract.class);
    }
}
