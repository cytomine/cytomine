package org.cytomine.repository.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> tomcatMaxParameterCount(
        @Value("${server.tomcat.max-parameter-count:100000}") int maxParameterCount
    ) {
        return factory -> factory.addConnectorCustomizers(connector ->
            connector.setMaxParameterCount(maxParameterCount));
    }
}