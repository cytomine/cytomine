package be.cytomine.config;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyCloakConfiguration {

    @Value("${keycloak-client.admin.user}")
    String user;
    @Value("${keycloak-client.admin.password}")
    String password;
    @Value("${keycloak-client.client-id}")
    String clientId;
    @Value("${keycloak-client.admin.realm}")
    String realm;
    @Value("${keycloak-client.url}")
    String url;

    @Bean
    Keycloak keycloak() {
        return Keycloak.getInstance(
                url,
                realm,
                user,
                password,
                clientId);
    }

}
