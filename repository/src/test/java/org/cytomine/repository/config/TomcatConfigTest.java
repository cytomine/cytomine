package org.cytomine.repository.config;

import java.io.IOException;
import java.util.List;

import org.apache.catalina.connector.Connector;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TomcatConfigTest {

    private static final String MAX_PARAMETER_COUNT_PROPERTY = "server.tomcat.max-parameter-count";

    @Test
    void tomcatMaxParameterCountSetsConnectorValue() {
        TomcatConfig config = new TomcatConfig();

        ConfigurableTomcatWebServerFactory factory = mock(ConfigurableTomcatWebServerFactory.class);
        WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> customizer =
            config.tomcatMaxParameterCount(100000);
        customizer.customize(factory);

        ArgumentCaptor<TomcatConnectorCustomizer> captor = ArgumentCaptor.forClass(TomcatConnectorCustomizer.class);
        verify(factory).addConnectorCustomizers(captor.capture());

        Connector connector = new Connector();
        captor.getValue().customize(connector);
        assertEquals(100000, connector.getMaxParameterCount());
    }

    @Test
    void applicationYamlDefinesThePropertyTomcatConfigReads() throws IOException {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
            .load("application.yml", new ClassPathResource("application.yml"));
        boolean found = sources.stream()
            .anyMatch(source -> source.getProperty(MAX_PARAMETER_COUNT_PROPERTY) != null);
        assertTrue(found, "application.yml must define '" + MAX_PARAMETER_COUNT_PROPERTY + "'");
    }
}