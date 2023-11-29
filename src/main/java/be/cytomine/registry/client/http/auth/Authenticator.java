package io.github.ya_b.registry.client.http.auth;

import io.github.ya_b.registry.client.exception.RegistryException;
import io.github.ya_b.registry.client.http.HttpClient;
import io.github.ya_b.registry.client.http.HttpHeaders;
import io.github.ya_b.registry.client.name.Reference;
import io.github.ya_b.registry.client.utils.JsonUtil;
import kotlin.Pair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Authenticator {
    private static final String SCOPE = "repository:%s:%s";
    private AuthUrl authUrl = new AuthUrl("https://auth.docker.io/token", "registry.docker.io");
    public static final String DOCKER_DOMAIN = "docker.io";
    private final Cache<Credential> basicCredential = new Cache<>(1000);
    private final Cache<DockerAuthResp> dockerToken = new Cache<>(1);
    private Credential dockerCredential = null;

    private Authenticator() {}

    private static final class AuthenticatorHolder {
        static final Authenticator authenticator = new Authenticator();
    }

    public static Authenticator instance() {
        return AuthenticatorHolder.authenticator;
    }

    public void setAuthUrl(AuthUrl authUrl) {
        this.authUrl = authUrl;
    }

    public synchronized void basic(String endpoint, Credential credential) {
        basicCredential.put(HttpClient.domainFrom(endpoint), new Pair<>(null, credential));
    }

    public void docker(Credential credential) {
        dockerCredential = credential;
    }

    private Credential getCredential(String endpoint) {
        String domain = HttpClient.domainFrom(endpoint);
        if (domain.endsWith(DOCKER_DOMAIN)) {
            return dockerCredential;
        }
        Pair<Long, Credential> pair = basicCredential.get(domain);
        return pair.getSecond();
    }

    @SafeVarargs
    public final String getToken(Pair<Scope, Reference>... pairs) throws RegistryException {
        Pair<Scope, Reference> pair = pairs[0];
        Credential credential = getCredential(pair.getSecond().getEndpoint());
        if (credential != dockerCredential) {
            return Credentials.basic(credential.getUsername(), credential.getPassword());
        }
        List<String> scopes = Arrays.stream(pairs)
                .map(p -> String.format(SCOPE, p.getSecond().getName(), p.getFirst().getScope()))
                .collect(Collectors.toList());
        String token = Optional.ofNullable(dockerToken.get(String.join("", scopes))).map(Pair::getSecond).map(DockerAuthResp::getToken).orElse(null);
        if (token == null) {
            Map<String, String> headers = new HashMap<>(2);
            if (dockerCredential != null) {
                headers.put(HttpHeaders.AUTHORIZATION, Credentials.basic(dockerCredential.getUsername(), dockerCredential.getPassword()));
            }
            try (Response response = HttpClient.execute(HttpClient.METHOD_GET, authUrl.scopes(scopes), Headers.of(headers), null)) {
                if (response.isSuccessful() && response.body() != null) {
                    DockerAuthResp resp = JsonUtil.fromJson(response.body().string(), DockerAuthResp.class);
                    dockerToken.put(String.join("", scopes), new Pair<>(System.currentTimeMillis() + (resp.getExpiresIn() * 1000), resp));
                    token = resp.getToken();
                }
            } catch (IOException e) {
                throw new RegistryException("Unauthorized:" + e.getMessage());
            }
        }
        if (token == null) {
            throw new RegistryException("Unauthorized");
        }
        return "Bearer " + token;
    }

    private static class Cache<T> extends LinkedHashMap<String, Pair<Long, T>> {
        private final int maxSize;

        public Cache(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public Pair<Long, T> get(Object key) {
            this.cleanExpired();
            return super.get(key);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Pair<Long, T>> eldest) {
            return size() > maxSize;
        }

        private void cleanExpired() {
            entrySet().stream()
                    .filter(e -> e.getValue().getFirst() != null && e.getValue().getFirst() > 0
                            && e.getValue().getFirst() < System.currentTimeMillis())
                    .map(Map.Entry::getKey)
                    .forEach(this::remove);
        }
    }

    @AllArgsConstructor
    public static class AuthUrl {
        private String url;
        private String service;

        public String scopes(List<String> scopes) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(url);
            stringBuilder.append("?service=");
            stringBuilder.append(service);
            for (String scope: scopes) {
                stringBuilder.append("&scope=").append(scope);
            }
            return stringBuilder.toString();
        }
    }
}
