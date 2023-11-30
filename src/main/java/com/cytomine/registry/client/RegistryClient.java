package be.cytomine.registry.client;

import be.cytomine.registry.client.http.auth.Authenticator;
import be.cytomine.registry.client.http.auth.Credential;
import be.cytomine.registry.client.http.auth.Scope;
import be.cytomine.registry.client.http.resp.CatalogResp;
import be.cytomine.registry.client.image.Context;
import be.cytomine.registry.client.manager.FileManager;
import be.cytomine.registry.client.manager.RegistryManager;
import be.cytomine.registry.client.name.Reference;
import jakarta.persistence.criteria.CriteriaBuilder;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

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

    public static void unAuthenticatedPush(String filePath, String image) throws IOException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(Paths.get(filePath)))) {
            unAuthenticatedPush(is, image);
        }
    }

//    @Value("${registry.url}")
//    private static String registryUrl;
//
//    public static void main(String[] args) {
//        try {
//            unAuthenticatedPush("/home/siddiq/AppEngineAppBundle/tasks/postomine.tar", registryUrl + "/postomine:1.2.9");
//            List<String> tags = tags(registryUrl + "/postomine");
//            for (String tag : tags) {
//                System.out.println(tag);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static void push(InputStream is, String image) throws IOException {
        Reference reference = Reference.parse(image);
        Context context = FILE_OPERATE.load(is);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL_PUSH, reference)));
        REGISTRY_OPERATE.push(context, reference);
    }

    public static void unAuthenticatedPush(InputStream is, String image) throws IOException {
        Reference reference = Reference.parse(image);
        Context context = FILE_OPERATE.load(is);
        REGISTRY_OPERATE.push(context, reference);
    }

    public static void validate(InputStream is) throws IOException
    {
        Context context = FILE_OPERATE.load(is);
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

    public static void unauthenticatedPull(String image, String filePath) throws IOException {
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            unauthenticatedPull(image, os);
        }
    }


    public static void unauthenticatedPull(String image, OutputStream outputStream) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        REGISTRY_OPERATE.load(context, reference);
        FILE_OPERATE.save(context, outputStream);
    }

    public static Optional<String> digest(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, reference)));
        return REGISTRY_OPERATE.digest(context, reference);
    }

    public static Optional<String> unauthenticatedDigest(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        return REGISTRY_OPERATE.digest(context, reference);
    }

    public static List<String> tags(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, reference)));
        return REGISTRY_OPERATE.tags(context, reference);

    }

    public static List<String> unauthenticatedTags(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        return REGISTRY_OPERATE.tags(context, reference);

    }

    public static void delete(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.DELETE, reference)));
        REGISTRY_OPERATE.delete(context, reference);
    }

    public static void unauthenticatedDelete(String image) throws IOException {
        Context context = new Context();
        Reference reference = Reference.parse(image);
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

    public static void unauthenticatedCopy(String src, String dst) throws IOException {
        Context context = new Context();
        Reference srcReference = Reference.parse(src);
        Reference dstReference = Reference.parse(dst);
        if (srcReference.getEndpoint().endsWith(Authenticator.DOCKER_DOMAIN) && dstReference.getEndpoint().endsWith(Authenticator.DOCKER_DOMAIN)) {
            REGISTRY_OPERATE.load(context, srcReference);
        } else {
            REGISTRY_OPERATE.load(context, srcReference);
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

    public static CatalogResp unauthenticatedCatalog(String url, Integer count, String last) throws IOException {
        Reference reference = new Reference();
        reference.setEndpoint(url);
        Context context = new Context();
        context.setReference(reference);
        return REGISTRY_OPERATE.catalog(context, count, last);
    }


    public static int unauthenticatedVersion() throws IOException {

        return REGISTRY_OPERATE.getVersion();
    }

}
