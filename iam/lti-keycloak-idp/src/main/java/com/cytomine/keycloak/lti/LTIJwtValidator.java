package com.cytomine.keycloak.lti;
import org.jboss.logging.Logger;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.SSLParameters;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.keycloak.util.JsonSerialization;


public class LTIJwtValidator {

    private static final Logger log = Logger.getLogger(LTIJwtValidator.class);

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final Map<String, CachedJwks> CACHE = new ConcurrentHashMap<>();

    private static final int FETCH_MAX_ATTEMPTS = 3;
    private static final Duration FETCH_RETRY_DELAY = Duration.ofMillis(300);

    // review later
    private static final HttpClient HTTP;
    static {
        try {

            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509ExtendedTrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    public void checkClientTrusted(X509Certificate[] certs, String authType, Socket socket) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType, Socket socket) {}
                    public void checkClientTrusted(X509Certificate[] certs, String authType, SSLEngine engine) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType, SSLEngine engine) {}
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());


            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("");

            HTTP = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize dev-mode trust-all HttpClient", e);
        }
    }

    private record CachedJwks(Map<String, JWK> keysByKid, Instant fetchedAt) {
        boolean isExpired() {
            return Instant.now().isAfter(fetchedAt.plus(CACHE_TTL));
        }
    }


    public JWSInput verify(String rawJwt, String jwksUrl) throws JWSInputException {
        JWSInput jws = new JWSInput(rawJwt);
        String kid = jws.getHeader().getKeyId();
        if (kid == null) {
            throw new IllegalArgumentException("Launch JWT is missing a 'kid' header - cannot select verification key");
        }

        JWK key = resolveKey(jwksUrl, kid);
        if (key == null) {
            // key rotation edge case: force a refresh once before giving up
            key = resolveKey(jwksUrl, kid, true);
        }
        if (key == null) {
            throw new IllegalArgumentException("No matching key for kid=" + kid + " in platform JWKS " + jwksUrl);
        }

        PublicKey publicKey = JWKParser.create(key).toPublicKey();
        boolean valid = verifySignature(jws, publicKey);
        if (!valid) {
            throw new IllegalArgumentException("Launch JWT signature verification failed");
        }
        return jws;
    }


    private boolean verifySignature(JWSInput jws, PublicKey publicKey) {
        try {
            String alg = jws.getHeader().getRawAlgorithm();
            String javaAlg = switch (alg) {
                case "RS256" -> "SHA256withRSA";
                case "RS384" -> "SHA384withRSA";
                case "RS512" -> "SHA512withRSA";
                case "ES256" -> "SHA256withECDSA";
                case "ES384" -> "SHA384withECDSA";
                case "ES512" -> "SHA512withECDSA";
                default -> throw new IllegalArgumentException("Unsupported JWS alg: " + alg);
            };
            java.security.Signature sig = java.security.Signature.getInstance(javaAlg);
            sig.initVerify(publicKey);
            sig.update(jws.getEncodedSignatureInput().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return sig.verify(jws.getSignature());
        } catch (Exception e) {
            log.warn("Signature verification error", e);
            return false;
        }
    }

    private JWK resolveKey(String jwksUrl, String kid) {
        return resolveKey(jwksUrl, kid, false);
    }

    private JWK resolveKey(String jwksUrl, String kid, boolean forceRefresh) {
        CachedJwks cached = CACHE.get(jwksUrl);
        if (forceRefresh || cached == null || cached.isExpired()) {
            cached = fetch(jwksUrl);
            CACHE.put(jwksUrl, cached);
        }
        return cached.keysByKid().get(kid);
    }

    @SuppressWarnings("unchecked")
    private CachedJwks fetch(String jwksUrl) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= FETCH_MAX_ATTEMPTS; attempt++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(jwksUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
                HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200) {
                    throw new RuntimeException("JWKS fetch failed: HTTP " + resp.statusCode() + " from " + jwksUrl);
                }
                Map<String, Object> doc = JsonSerialization.readValue(resp.body(), Map.class);
                Object keysObj = doc.get("keys");
                Map<String, JWK> byKid = new ConcurrentHashMap<>();
                if (keysObj instanceof Iterable<?> keys) {
                    for (Object k : keys) {
                        String raw = JsonSerialization.writeValueAsString(k);
                        JWK jwk = JsonSerialization.readValue(raw, JWK.class);
                        if (jwk.getKeyId() != null) {
                            byKid.put(jwk.getKeyId(), jwk);
                        }
                    }
                }
                return new CachedJwks(byKid, Instant.now());
            } catch (Exception e) {
                lastFailure = e;
                if (attempt < FETCH_MAX_ATTEMPTS) {
                    log.warnf("JWKS fetch attempt %d/%d failed for %s (%s) - retrying shortly",
                        attempt, FETCH_MAX_ATTEMPTS, jwksUrl, e.getMessage());
                    try {
                        Thread.sleep(FETCH_RETRY_DELAY.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.warn("Failed to fetch/parse platform JWKS from " + jwksUrl + " after "
            + FETCH_MAX_ATTEMPTS + " attempts", lastFailure);
        return new CachedJwks(Map.of(), Instant.now());
    }
}