package org.cytomine.resourceserver;

import org.cytomine.common.serialization.ObjectMapperFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"org.cytomine.resourceserver", "be.cytomine.common.mapper",
    "be.cytomine.common.config.security", "be.cytomine.common.serialization"})
@Import({ObjectMapperFactory.class})
@ConfigurationPropertiesScan
public class ResourceServer {
    public static void main(String[] args) {
        SpringApplication.run(ResourceServer.class, args);
    }
}