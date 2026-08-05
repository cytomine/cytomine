package com.cytomine.keycloak.lti;
import org.jboss.logging.Logger;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;

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

/**
 * Fetches a platform's JWKS document and verifies the signature on an LTI
 * launch id_token against it.
 *
 * This is intentionally minimal: a tiny in-memory cache keyed by JWKS URL,
 * refreshed every 10 minutes. For production, back this with Keycloak's
 * shared infinispan cache instead of a static map so it works correctly
 * across a clustered deployment.
 */
public class LTIJwtValidator {

    private static final Logger log = Logger.getLogger(LTIJwtValidator.class);

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final Map<String, CachedJwks> CACHE = new ConcurrentHashMap<>();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private record CachedJwks(Map<String, JWK> keysByKid, Instant fetchedAt) {
        boolean isExpired() {
            return Instant.now().isAfter(fetchedAt.plus(CACHE_TTL));
        }
    }

    /**
     * Verifies the JWS signature on rawJwt using the given platform JWKS URL.
     * Returns the parsed JWSInput (already signature-verified) so the caller
     * can go on to check claims (iss, aud, nonce, deployment_id, exp, ...).
     *
     * Throws JWSInputException / RuntimeException on any failure - callers
     * must treat any exception here as "reject the launch".
     */
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

    /**
     * Verifies the JWS signature using plain java.security, so this doesn't
     * depend on internal Keycloak verifier classes that vary by version.
     * Assumes RS256/RS384/RS512 or ES256/ES384/ES512 - LTI 1.3 platforms are
     * required to support RS256 at minimum, which covers the common case.
     */
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
            log.warn("Failed to fetch/parse platform JWKS from " + jwksUrl, e);
            return new CachedJwks(Map.of(), Instant.now());
        }
    }
}
