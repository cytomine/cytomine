package org.cytomine.authorizationapi;

import org.cytomine.common.serialization.ObjectMapperFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"org.cytomine.authorizationapi", "be.cytomine.common.mapper",
    "be.cytomine.common.config.security", "be.cytomine.common.serialization"})
@Import({ObjectMapperFactory.class})
@ConfigurationPropertiesScan
public class AuthorizationApiApp {
    public static void main(String[] args) {
        SpringApplication.run(AuthorizationApiApp.class, args);
    }
}
