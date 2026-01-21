package com.cytomine.registry.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.cytomine.registry.client.http.resp.CatalogResp;

public class RegistryClientTest {
    private GenericContainer<?> registryContainer;
    private String registryUrl;

    @BeforeEach
    void setUp() throws IOException {
        registryContainer = new GenericContainer<>(DockerImageName.parse(TestConfig.REGISTRY_IMAGE))
                .withExposedPorts(5000)
                .withEnv("REGISTRY_STORAGE_DELETE_ENABLED", "true");
        registryContainer.start();

        registryUrl = String.format("http://%s:%d",
                registryContainer.getHost(),
                registryContainer.getMappedPort(5000));

        RegistryClient.config(registryUrl);

        ClassLoader classLoader = RegistryClientTest.class.getClassLoader();
        RegistryClient.push(classLoader.getResourceAsStream("postomine.tar"), "postomine:1.3");
    }

    @AfterEach
    void tearDown() {
        if (registryContainer != null) {
            registryContainer.stop();
        }
    }

    @Test
    void shouldReturnValidDigestForExistingImage() throws Exception {
        Optional<String> digest = RegistryClient.digest("postomine:1.3");
        Assertions.assertTrue(digest.get().startsWith("sha256:"));
    }

    @Test
    void shouldReturnAllTagsForRepository() throws Exception {
        List<String> tags = RegistryClient.tags("postomine");

        Assertions.assertFalse(tags.isEmpty(), "Tags list should not be empty");
        Assertions.assertTrue(tags.contains("1.3"));
    }

    @Test
    void shouldPullImageByDigestAndPushWithNewTag() throws Exception {
        Optional<String> originalDigest = RegistryClient.digest("postomine:1.3");
        Assertions.assertTrue(originalDigest.isPresent());

        Path path = Files.createTempFile(UUID.randomUUID().toString(), ".tar");

        try {
            RegistryClient.pull("postomine@" + originalDigest.get(), path.toString());
            Assertions.assertTrue(Files.exists(path));

            InputStream stream = Files.newInputStream(path);

            RegistryClient.push(stream, "postomine:copied");

            Optional<String> copiedDigest = RegistryClient.digest("postomine:copied");

            Assertions.assertTrue(copiedDigest.isPresent());
            Assertions.assertTrue(copiedDigest.get().startsWith("sha256:"));
            Assertions.assertEquals(originalDigest.get(), copiedDigest.get());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void shouldCopyImageFromDigestToNewRepository() throws Exception {
        Optional<String> originalDigest = RegistryClient.digest("postomine:1.3");
        Assertions.assertTrue(originalDigest.isPresent());

        String sourceImage = "postomine@" + originalDigest.get();
        String targetImage = "postomine-copy:latest";

        RegistryClient.copy(sourceImage, targetImage);

        Optional<String> copiedDigest = RegistryClient.digest(targetImage);

        Assertions.assertTrue(copiedDigest.isPresent());
        Assertions.assertEquals(originalDigest.get(), copiedDigest.get());
    }

    @Test
    void shouldReturnCatalogWithRepositories() throws IOException {
        int pageSize = 10;
        String lastRepository = "";

        CatalogResp catalogResp = RegistryClient.catalog(registryUrl, pageSize, lastRepository);

        Assertions.assertNotNull(catalogResp);
        Assertions.assertNotNull(catalogResp.getRepositories());
        Assertions.assertTrue(catalogResp.getRepositories().contains("postomine"));
    }
}
