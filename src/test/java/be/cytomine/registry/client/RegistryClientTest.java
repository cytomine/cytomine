package io.github.ya_b.registry.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.github.ya_b.registry.client.http.resp.CatalogResp;
import io.github.ya_b.registry.client.utils.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class RegistryClientTest {

    @BeforeAll
    static void auth() throws IOException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger("ROOT");
        logger.setLevel(Level.DEBUG);
        RegistryClient.authBasic("localhost:5000", "admin", "123456");
        RegistryClient.authDockerHub(System.getenv("DOCKER_USERNAME"), System.getenv("DOCKER_PASSWORD"));
    }

    @Test
    void digest() throws Exception {
        Optional<String> digest = RegistryClient.digest("registry@sha256:ce14a6258f37702ff3cd92232a6f5b81ace542d9f1631966999e9f7c1ee6ddba");
        Assertions.assertTrue(digest.get().startsWith("sha256:"));
    }

    @Test
    void tags() throws Exception {
        List<String> tags = RegistryClient.tags("registry");
        Assertions.assertTrue(tags.contains("latest"));
    }

    @Test
    void dockerIOPullPush() throws IOException {
        Path path = Files.createTempFile(UUID.randomUUID().toString(), ".tar");
        RegistryClient.pull("registry@sha256:cc6393207bf9d3e032c4d9277834c1695117532c9f7e8c64e7b7adcda3a85f39", path.toString());
        Assertions.assertTrue(Files.exists(path));
        RegistryClient.push(path.toString(), System.getenv("DOCKER_USERNAME") + "/registry");
        Assertions.assertTrue(RegistryClient.digest(System.getenv("DOCKER_USERNAME") + "/registry").isPresent());

    }

    @Test
    void dockerIOCopy() throws IOException {
        RegistryClient.copy("registry@sha256:712c58f0d738ba95788d2814979028fd648a37186ae0dd4141f786125ba6d680",
                System.getenv("DOCKER_USERNAME") + "/registry");
        Assertions.assertTrue(RegistryClient.digest(System.getenv("DOCKER_USERNAME") + "/registry").isPresent());
    }

    @Test
    @Disabled
    void registryPullPush() throws IOException {
        Path path = Files.createTempFile(UUID.randomUUID().toString(), ".tar");
        RegistryClient.pull("localhost:5000/registry:latest", path.toString());
        Assertions.assertTrue(Files.exists(path));
        RegistryClient.push(path.toString(), "localhost:5000/test:v2");
        Assertions.assertEquals(
                RegistryClient.digest("localhost:5000/registry:latest").get(),
                RegistryClient.digest("localhost:5000/test:v2").get()
        );
        RegistryClient.delete("localhost:5000/test@" + RegistryClient.digest("localhost:5000/test:v2").get());
        Files.delete(path);
    }


    @Test
    @Disabled
    void registryCopy() throws IOException {
        RegistryClient.copy("localhost:5000/registry:latest", "localhost:5000/test:v1");
        Assertions.assertEquals(
                RegistryClient.digest("localhost:5000/registry:latest").get(),
                RegistryClient.digest("localhost:5000/test:v1").get()
        );

        RegistryClient.copy("localhost:5000/test:v1", "localhost:5000/test:v2");
        Assertions.assertEquals(
                RegistryClient.digest("localhost:5000/test:v1").get(),
                RegistryClient.digest("localhost:5000/test:v2").get()
        );
        RegistryClient.delete("localhost:5000/test@" + RegistryClient.digest("localhost:5000/test:v1").get());
    }

    @Test
    @Disabled
    void registryCatalog() {
        Assertions.assertDoesNotThrow(() -> {
            CatalogResp catalogResp = RegistryClient.catalog("http://localhost:5000", 10, "test");
            System.out.println(JsonUtil.toJson(catalogResp));
            Assertions.assertNotNull(catalogResp);
        });
    }
}