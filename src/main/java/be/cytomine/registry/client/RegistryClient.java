package io.github.ya_b.registry.client;

import io.github.ya_b.registry.client.http.auth.Authenticator;
import io.github.ya_b.registry.client.http.auth.Credential;
import io.github.ya_b.registry.client.http.auth.Scope;
import io.github.ya_b.registry.client.http.resp.CatalogResp;
import io.github.ya_b.registry.client.image.Context;
import io.github.ya_b.registry.client.manager.FileManager;
import io.github.ya_b.registry.client.manager.RegistryManager;
import io.github.ya_b.registry.client.name.Reference;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Slf4j
public class RegistryClient {

    private static final FileManager FILE_OPERATE = new FileManager();

    private static final RegistryManager REGISTRY_OPERATE = new RegistryManager();
    
    private static final Authenticator AUTHENTICATOR = Authenticator.instance();

    public static void authBasic(String endpoint, String username, String password) {
        AUTHENTICATOR.basic(endpoint, new Credential(username, password));
    }

    public static void authDockerHub(String username, String password) {
        AUTHENTICATOR.docker(new Credential(username, password));
    }

    public static void push(String filePath, String image) throws IOException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(Paths.get(filePath)))) {
            push(is, image);
        }
    }

    public static void push(InputStream is, String image) throws IOException {
        Reference reference = Reference.parse(image);
        Context context = FILE_OPERATE.load(is);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL_PUSH, reference)));
        REGISTRY_OPERATE.push(context, reference);
    }

    public static void pull(String image, String filePath) throws IOException {
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            pull(image, os);
        }
    }

    public static void pull(String image, OutputStream outputStream) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, reference)));
        REGISTRY_OPERATE.load(context, reference);
        FILE_OPERATE.save(context, outputStream);
    }

    public static Optional<String> digest(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, reference)));
        return REGISTRY_OPERATE.digest(context, reference);
    }

    public static List<String> tags(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, reference)));
        return REGISTRY_OPERATE.tags(context, reference);

    }

    public static void delete(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.DELETE, reference)));
        REGISTRY_OPERATE.delete(context, reference);
    }

    public static void copy(String src, String dst) throws IOException {
        Context context = new Context();
        Reference srcReference = Reference.parse(src);
        Reference dstReference = Reference.parse(dst);
        if (srcReference.getEndpoint().endsWith(Authenticator.DOCKER_DOMAIN) && dstReference.getEndpoint().endsWith(Authenticator.DOCKER_DOMAIN)) {
            context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, srcReference), new Pair<>(Scope.PULL_PUSH, dstReference)));
            REGISTRY_OPERATE.load(context, srcReference);
        } else {
            context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, srcReference)));
            REGISTRY_OPERATE.load(context, srcReference);
            context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL_PUSH, srcReference)));
        }
        REGISTRY_OPERATE.copy(context, dst);
    }

    public static CatalogResp catalog(String url, Integer count, String last) throws IOException {
        Reference reference = new Reference();
        reference.setEndpoint(url);
        Context context = new Context();
        context.setReference(reference);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.NONE, reference)));
        return REGISTRY_OPERATE.catalog(context, count, last);
    }
}
