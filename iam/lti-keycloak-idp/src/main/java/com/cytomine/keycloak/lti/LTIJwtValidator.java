package com.cytomine.keycloak.lti;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Fetches a platform's JWKS document on demand and verifies the signature
 * on an LTI launch id_token against it using Keycloak's HttpClientProvider.
 */
public class LTIJwtValidator {

    private static final Logger log = Logger.getLogger(LTIJwtValidator.class);

    private static final int FETCH_MAX_ATTEMPTS = 3;
    private static final Duration FETCH_RETRY_DELAY = Duration.ofMillis(300);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int SOCKET_TIMEOUT_MS = 5000;

    private final KeycloakSession session;

    /**
     * Inject the KeycloakSession to access the HttpClientProvider SPI.
     */
    public LTIJwtValidator(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Verifies the JWS signature on rawJwt using keys freshly fetched from the given jwksUrl.
     */
    public JWSInput verify(String rawJwt, String jwksUrl) throws JWSInputException {
        JWSInput jws = new JWSInput(rawJwt);
        String kid = jws.getHeader().getKeyId();
        if (kid == null) {
            throw new IllegalArgumentException("Launch JWT is missing a 'kid' header - cannot select verification key");
        }

        // Fetch fresh keys using Keycloak's HttpClientProvider
        Map<String, JWK> keysByKid = fetch(jwksUrl);
        JWK key = keysByKid.get(kid);

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
            sig.update(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8));
            return sig.verify(jws.getSignature());
        } catch (Exception e) {
            log.warn("Signature verification error", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, JWK> fetch(String jwksUrl) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= FETCH_MAX_ATTEMPTS; attempt++) {
            try {
                String body = fetchBody(jwksUrl);
                Map<String, Object> doc = JsonSerialization.readValue(body, Map.class);
                Object keysObj = doc.get("keys");
                Map<String, JWK> byKid = new HashMap<>();
                if (keysObj instanceof Iterable<?> keys) {
                    for (Object k : keys) {
                        String raw = JsonSerialization.writeValueAsString(k);
                        JWK jwk = JsonSerialization.readValue(raw, JWK.class);
                        if (jwk.getKeyId() != null) {
                            byKid.put(jwk.getKeyId(), jwk);
                        }
                    }
                }
                return byKid;
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
        throw new RuntimeException("Failed to fetch/parse platform JWKS from " + jwksUrl + " after "
            + FETCH_MAX_ATTEMPTS + " attempts", lastFailure);
    }

    private String fetchBody(String jwksUrl) throws Exception {
        HttpClientProvider clientProvider = session.getProvider(HttpClientProvider.class);
        if (clientProvider == null) {
            throw new IllegalStateException("HttpClientProvider is not available in KeycloakSession");
        }

        HttpGet request = new HttpGet(jwksUrl);

        // Configure per-request connection and socket timeouts
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT_MS)
            .setSocketTimeout(SOCKET_TIMEOUT_MS)
            .setRedirectsEnabled(true)
            .build();
        request.setConfig(requestConfig);

        try (CloseableHttpResponse response = clientProvider.getHttpClient().execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("JWKS fetch failed: HTTP " + statusCode + " from " + jwksUrl);
            }
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }
}