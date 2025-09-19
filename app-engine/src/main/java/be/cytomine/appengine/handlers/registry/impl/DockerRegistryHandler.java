package be.cytomine.appengine.handlers.registry.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import io.github.ya_b.registry.client.RegistryClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.cytomine.appengine.exceptions.RegistryException;
import be.cytomine.appengine.handlers.RegistryHandler;

@Component
@Slf4j
public class DockerRegistryHandler implements RegistryHandler {

    @Value("${registry.url}")
    private String registryHost;

    @Value("${registry.user}")
    private Optional<String> registryUsername = Optional.empty();

    @Value("${registry.password}")
    private Optional<String> registryPassword = Optional.empty();

    @PostConstruct
    void init() throws IOException {
        if (registryUsername.filter(e -> !e.isBlank()).isPresent()) {
            RegistryClient.authBasic(registryHost, registryUsername.get(),
                registryPassword.orElseThrow(() -> new IllegalArgumentException("Username was "
                    + "provided for registry but not password")));
        }

        log.info("Docker Registry Handler: initialised");
    }

    @Override
    public void pushImage(InputStream imageInputStream, String imageName) throws RegistryException {
        log.info("Docker Registry Handler: pushing image...");
        try {

            RegistryClient.push(String.valueOf(imageInputStream), imageName);
            log.info("Docker Registry Handler: image pushed");
        } catch (FileNotFoundException e) {
            log.error("Image data file not found: {}", imageName, e);
            throw new RegistryException("Docker Registry Handler: image data file not found");
        } catch (IOException e) {
            log.error("Error reading image data from file: {}", imageName, e);
            throw new RegistryException("Docker Registry Handler: failed to read image data");
        } catch (Exception e) {
            log.error("Failed to push image: {}", imageName, e);
            String message = "Docker Registry Handler: failed to push the image to registry";
            throw new RegistryException(message);
        }
    }

    @Override
    public void deleteImage(String imageName) throws RegistryException {
        try {
            RegistryClient.delete(imageName);
        } catch (IOException e) {
            log.error("Error reading image data from file: {}", imageName, e);
            throw new RegistryException("Docker Registry Handler: "
                + "failed to delete image from registry");
        }
    }
}
