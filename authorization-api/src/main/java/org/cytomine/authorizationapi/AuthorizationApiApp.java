package org.cytomine.authorizationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {
    "org.cytomine.authorizationapi",
    "org.cytomine.common.mapper",
    "org.cytomine.common.config.security",
    "org.cytomine.common.security",
    "org.cytomine.common.serialization",
    " org.cytomine.common.beans.authorization.internal"
})
@ConfigurationPropertiesScan(basePackages = {
    "org.cytomine.authorizationapi",
    "org.cytomine.common.beans.authorization.internal",
    "org.cytomine.common.serialization",
})
public class AuthorizationApiApp {
    public static void main(String[] args) {
        SpringApplication.run(AuthorizationApiApp.class, args);
    }
}
