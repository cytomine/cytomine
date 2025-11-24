package be.cytomine.appengine.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {
    @Bean
    public KubernetesClient kubernetesClient() {
        Config configurations = new ConfigBuilder()
                                    .withTrustCerts(true)
                                    .build();

        return new KubernetesClientBuilder()
                   .withConfig(configurations)
                   .build();
    }
}
