package com.cytomine.registry.client.http;

<<<<<<< HEAD
=======
import okhttp3.Credentials;

>>>>>>> origin/main
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

<<<<<<< HEAD
import okhttp3.Credentials;

=======
>>>>>>> origin/main
public class CredentialsCache {
    private final static Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static void save(String endpoint, String username, String password) {
        CACHE.put(endpoint, Credentials.basic(username, password));
    }

    public static Optional<String> load(String endpoint) {
        return Optional.ofNullable(CACHE.get(endpoint));
    }
}
