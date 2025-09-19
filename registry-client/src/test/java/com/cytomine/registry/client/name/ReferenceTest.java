package com.cytomine.registry.client.name;

import com.cytomine.registry.client.config.Configurer;
import com.cytomine.registry.client.constant.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReferenceTest {

    @BeforeAll
    static void init()
    {
        Configurer configurer = Configurer.instance();
        Configurer.url("http:registry:5000");
    }

    @Test
    void parse() {
        Reference reference = Reference.parse("openjdk:17-jdk");
        Assertions.assertEquals("http://" + Constants.ENDPOINT_DEFAULT, reference.getEndpoint());
        Assertions.assertEquals("library/openjdk", reference.getName());
        Assertions.assertEquals("17-jdk", reference.getTag());
        Assertions.assertNull(reference.getDigest());
    }

    @Test
    void parse1() {
        Reference reference = Reference.parse("test/test:v1");
        Assertions.assertEquals("http://" + Constants.ENDPOINT_DEFAULT, reference.getEndpoint());
        Assertions.assertEquals("test/test", reference.getName());
        Assertions.assertEquals("v1", reference.getTag());
        Assertions.assertNull(reference.getDigest());
    }

    @Test
    void parse2() {
        Reference reference = Reference.parse("test/test:v1@sha256:b8604a3fe8543c9e6afc29550de05b36cd162a97aa9b2833864ea8a5be11f3e2");
        Assertions.assertEquals("http://" + Constants.ENDPOINT_DEFAULT, reference.getEndpoint());
        Assertions.assertEquals("test/test", reference.getName());
        Assertions.assertEquals("v1", reference.getTag());
        Assertions.assertEquals("sha256:b8604a3fe8543c9e6afc29550de05b36cd162a97aa9b2833864ea8a5be11f3e2", reference.getDigest());
    }

    @Test
    void parse3() {
        Reference reference = Reference.parse("test/test");
        Assertions.assertEquals("http://" + Constants.ENDPOINT_DEFAULT, reference.getEndpoint());
        Assertions.assertEquals("test/test", reference.getName());
        Assertions.assertEquals("latest", reference.getTag());
        Assertions.assertNull(reference.getDigest());
    }

    @Test
    void parse4() {
        Reference reference = Reference.parse("test/test@sha256:b8604a3fe8543c9e6afc29550de05b36cd162a97aa9b2833864ea8a5be11f3e2");
        Assertions.assertEquals("http://" + Constants.ENDPOINT_DEFAULT, reference.getEndpoint());
        Assertions.assertEquals("test/test", reference.getName());
        Assertions.assertEquals("latest", reference.getTag());
        Assertions.assertEquals("sha256:b8604a3fe8543c9e6afc29550de05b36cd162a97aa9b2833864ea8a5be11f3e2", reference.getDigest());
    }

    @Test
    void parse5() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Reference.parse("test/test:v1@b8604a3fe8543c9e6afc29550de05b36cd162a97aa9b2833864ea8a5be11f3e2"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Reference.parse("test/test:v1@sha256"));
    }
}
