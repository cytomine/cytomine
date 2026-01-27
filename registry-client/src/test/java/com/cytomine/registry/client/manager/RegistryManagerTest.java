package com.cytomine.registry.client.manager;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.cytomine.registry.client.image.Context;
import com.cytomine.registry.client.name.Reference;
import com.cytomine.registry.client.RegistryClient;
import com.cytomine.registry.client.RegistryClientTest;
import com.cytomine.registry.client.TestConfig;

public class RegistryManagerTest {

    private final RegistryManager REGISTRY_OPERATE = new RegistryManager();

    @BeforeEach
    void init() throws IOException {
        GenericContainer<?> registryContainer = new GenericContainer<>(DockerImageName.parse(TestConfig.REGISTRY_IMAGE))
                .withExposedPorts(TestConfig.REGISTRY_PORT)
                .withEnv("REGISTRY_STORAGE_DELETE_ENABLED", "true");
        registryContainer.start();

        String registryUrl = String.format("http://%s:%d",
                registryContainer.getHost(),
                registryContainer.getMappedPort(TestConfig.REGISTRY_PORT));

        RegistryClient.config(registryUrl);

        ClassLoader classLoader = RegistryClientTest.class.getClassLoader();
        RegistryClient.push(classLoader.getResourceAsStream("postomine.tar"), "postomine:1.3");
    }

    @Test
    void shouldLoadCorrectDigest() throws Exception {
        Context context = new Context();
        Reference reference = Reference.prepareReference("postomine:1.3");

        REGISTRY_OPERATE.load(context, reference);

        String expectedDigest = "sha256:96b4c4806b2878c9e51a8036106b374834f28067a61331c47924a083054a0059";
        Assertions.assertEquals(expectedDigest, context.getConfig().getDigest());
        Assertions.assertEquals(2, context.getLayers().size());
    }
}
