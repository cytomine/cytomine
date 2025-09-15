package be.cytomine.appengine.config;

import be.cytomine.appengine.handlers.RegistryHandler;
import be.cytomine.appengine.handlers.StorageHandler;
import be.cytomine.appengine.handlers.registry.impl.DefaultRegistryHandler;
import be.cytomine.appengine.handlers.registry.impl.DockerRegistryHandler;
import be.cytomine.appengine.handlers.storage.impl.FileSystemStorageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ImplementationConfig {

    @Value("${storage.impl}")
    private String storageImplementationSelector;

    @Value("${scheduler.impl}")
    private String schedulerImplementationSelector;

    @Value("${registry.impl}")
    private String registryImplementationSelector;

    @Value("${registry-client.host}")
    private String registryHost;

    @Value("${registry-client.port}")
    private String registryPort;

    @Value("${registry-client.scheme}")
    private String registryScheme;

    @Value("${registry-client.authenticated}")
    private boolean authenticated;

    @Value("${registry-client.user}")
    private String registryUsername;

    @Value("${registry-client.password}")
    private String registryPassword;

    @Bean
    @Primary
    public StorageHandler loadStorageImpl() throws Exception {
        return new FileSystemStorageHandler();
    }

    @Bean
    @Primary
    public RegistryHandler loadRegistryImpl() throws Exception {
        if (registryImplementationSelector.equalsIgnoreCase("docker")) {
            return new DockerRegistryHandler(
                registryHost,
                registryPort,
                registryScheme,
                authenticated,
                registryUsername,
                registryPassword
            );
        }
        return new DefaultRegistryHandler();
    }
}
