package com.cytomine.registry.client;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.cytomine.registry.client.http.resp.CatalogResp;
import com.cytomine.registry.client.utils.JsonUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

class RegistryClientTest {
    private GenericContainer<?> registryContainer;

    @BeforeEach
    void setUp() throws IOException {
        String imageName = DockerImageName.parse("registry:2.8.3");
        registryContainer = new GenericContainer<>(imageName)
                .withExposedPorts(5000)
                .withEnv("REGISTRY_STORAGE_DELETE_ENABLED", "true");
        registryContainer.start();

        String registryUrl = String.format("http://%s:%d",
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
    void digest() throws Exception {
        Optional<String> digest = RegistryClient.digest("postomine:1.3");
        Assertions.assertTrue(digest.get().startsWith("sha256:"));
    }

    @Test
    void tags() throws Exception {
        List<String> tags = RegistryClient.tags("postomine");
        Assertions.assertTrue(tags.contains("1.3"));
    }

    @Test
    void dockerIOPullPush() throws IOException {
        Path path = Files.createTempFile(UUID.randomUUID().toString(), ".tar");
        RegistryClient.pull("registry@sha256" +
                ":cc6393207bf9d3e032c4d9277834c1695117532c9f7e8c64e7b7adcda3a85f39", path.toString());
        Assertions.assertTrue(Files.exists(path));
        InputStream stream = new ByteArrayInputStream(path.toString().getBytes());
        RegistryClient.push(stream, System.getenv("DOCKER_USERNAME") + "/registry");
        Assertions.assertTrue(RegistryClient.digest(System.getenv("DOCKER_USERNAME") + "/registry").isPresent());
    }

    @Test
    @Disabled
    void dockerIOCopy() throws IOException {
        RegistryClient.copy("registry@sha256" +
                ":712c58f0d738ba95788d2814979028fd648a37186ae0dd4141f786125ba6d680",
                System.getenv("DOCKER_USERNAME") + "/registry");
        Assertions.assertTrue(RegistryClient.digest(System.getenv("DOCKER_USERNAME") + "/registry").isPresent());
    }

    @Test
    @Disabled
    void registryPullPush() throws IOException {
        Path path = Files.createTempFile("postmine", ".tar");
        RegistryClient.pull("postomine:1.3", path.toString());
        Assertions.assertTrue(Files.exists(path));
        InputStream stream = new FileInputStream(path.toString());
        RegistryClient.push(stream, "postomine:1.4");
        Assertions.assertEquals(
                RegistryClient.digest("postomine:1.3").get(),
                RegistryClient.digest("postomine:1.4").get());
        Files.delete(path);
    }

    @Test
    @Disabled
    void registryCatalog() {
        Assertions.assertDoesNotThrow(() -> {
            CatalogResp catalogResp = RegistryClient.catalog("http://registry:5000", 10, "test");
            System.out.println(JsonUtil.toJson(catalogResp));
            Assertions.assertNotNull(catalogResp);
        });
    }
}
