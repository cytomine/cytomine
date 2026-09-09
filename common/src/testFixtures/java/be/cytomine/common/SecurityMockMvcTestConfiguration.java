package be.cytomine.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestConfiguration(proxyBeanMethods = false)
public class SecurityMockMvcTestConfiguration {

    @Bean
    public MockMvcBuilderCustomizer securityMockMvcBuilderCustomizer() {
        return builder -> builder.defaultRequest(
            MockMvcRequestBuilders.get("/").with(SecurityMockMvcRequestPostProcessors.jwt()));
    }
}
