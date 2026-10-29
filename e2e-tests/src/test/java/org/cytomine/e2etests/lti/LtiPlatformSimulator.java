package org.cytomine.e2etests.lti;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Thin stand-in for an LMS platform ("issuer") during wire-level tests. It emulates exactly the
 * three platform-side touchpoints of the LTI 1.3 deep-linking flow:
 *
 * <ul>
 *   <li>{@code GET /jwks} - the platform JWKS used by the IAM LTI provider to verify the signed
 *       {@code LtiDeepLinkingRequest} id_token this server issues,</li>
 *   <li>{@code GET /auth} - the platform's OIDC authorization endpoint, which answers the tool's
 *       auth request (state/nonce/redirect_uri) with an auto-submitting form that posts a signed
 *       id_token back to the tool's broker callback URL - what Moodle and Blackboard do for a
 *       user that is already authenticated on the platform ("prompt=none"),</li>
 *   <li>{@code POST /deep-link-return} - the {@code deep_link_return_url} to which the tool posts
 *       its {@code LtiDeepLinkingResponse} JWT.</li>
 * </ul>
 *
 * <p>The server listens on {@link LtiSettings#SIMULATOR_PORT}; the compose IAM container resolves
 * {@code localhost} to the host through its extra_hosts entry, so fixed {@code localhost} URLs are
 * valid configuration values for every Keycloak-side setting (platformJwksUrl, deep_link_return_url).</p>
 */
final class LtiPlatformSimulator {

    static final String AUTH_PATH = "/auth";
    static final String JWKS_PATH = "/jwks";
    static final String DEEP_LINK_RETURN_PATH = "/deep-link-return";

    static final String KEY_ID = "lti-sim-signing-key";

    private final RSAKey signingKey;
    private HttpServer server;

    private volatile PendingLaunch pendingLaunch;
    private volatile String lastPostedResponseJwt;

    LtiPlatformSimulator() {
        try {
            this.signingKey = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(KEY_ID)
                .algorithm(JWSAlgorithm.RS256)
                .generate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate the platform simulator signing key", e);
        }
    }

    void start() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(LtiSettings.SIMULATOR_PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.createContext("/", this::handle);
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException(
                "Could not start the platform simulator on port " + LtiSettings.SIMULATOR_PORT, e);
        }
    }

    void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    String authorizationEndpoint() {
        return LtiSettings.simulatorBase() + AUTH_PATH;
    }

    String deepLinkReturnUrl() {
        return LtiSettings.simulatorBase() + DEEP_LINK_RETURN_PATH;
    }

    String jwksUrl() {
        return LtiSettings.simulatorBase() + JWKS_PATH;
    }

    /**
     * Arms the simulator for the next launch: the claims map handed to this method is what
     * {@code /auth} will sign into the {@code LtiDeepLinkingRequest} id_token for that launch,
     * after the optional variant customization has been applied.
     */
    void beginLaunch(Map<String, Object> requestClaims, UnaryOperator<Map<String, Object>> variant) {
        Map<String, Object> finalClaims = variant == null
            ? new LinkedHashMap<>(requestClaims)
            : variant.apply(new LinkedHashMap<>(requestClaims));
        this.pendingLaunch = new PendingLaunch(finalClaims);
    }

    /** The JWT the tool posted to {@code deep_link_return_url} at the end of the last flow. */
    String lastPostedResponseJwt() {
        return lastPostedResponseJwt;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            if (JWKS_PATH.equals(path)) {
                String jwks = new JWKSet(signingKey.toPublicJWK()).toString();
                respond(exchange, 200, "application/json", jwks);
            } else if (AUTH_PATH.equals(path)) {
                respond(exchange, 200, "text/html", authorizationResponse(exchange));
            } else if (DEEP_LINK_RETURN_PATH.equals(path)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String jwt = formField(body, "JWT");
                if (jwt != null) {
                    this.lastPostedResponseJwt = jwt;
                }
                respond(exchange, 204, "text/html", "");
            } else {
                respond(exchange, 404, "text/plain", "unknown simulator route " + path);
            }
        } finally {
            exchange.close();
        }
    }

    private String authorizationResponse(HttpExchange exchange) throws IOException {
        PendingLaunch launch = pendingLaunch;
        if (launch == null) {
            throw new IllegalStateException("No armed launch - call beginLaunch before driving the flow");
        }
        Map<String, String> query = queryParameters(exchange);
        String state = query.get("state");
        String nonce = query.get("nonce");
        String redirectUri = query.get("redirect_uri");
        if (state == null || nonce == null || redirectUri == null) {
            throw new IllegalStateException(
                "Platform /auth called without state/nonce/redirect_uri: " + exchange.getRequestURI());
        }

        Map<String, Object> claims = new LinkedHashMap<>(launch.claims);
        claims.put("state", state);
        claims.put("nonce", nonce);
        long now = System.currentTimeMillis() / 1000;
        claims.put("exp", now + 300L);
        claims.put("iat", now);

        String jwt = signedRequestJwt(claims);
        return autoSubmitForm(redirectUri, "id_token", jwt, "state", state);
    }

    private String signedRequestJwt(Map<String, Object> claims) {
        Map<String, Object> ordered = new LinkedHashMap<>();
        List<String> extras = List.of("sub", "nonce", "state", "exp", "iat");
        for (String extra : extras) {
            if (claims.containsKey(extra)) {
                ordered.put(extra, claims.get(extra));
            }
        }
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            ordered.put(entry.getKey(), entry.getValue());
        }
        try {
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(net.minidev.json.JSONValue.toJSONString(ordered));
            SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY_ID).build(),
                claimsSet);
            signedJwt.sign(new RSASSASigner((PrivateKey) signingKey.toKeyPair().getPrivate()));
            return signedJwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign the platform request id_token", e);
        }
    }

    private static String autoSubmitForm(String action, String firstField, String firstValue,
                                         String secondField, String secondValue) {
        return "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n"
            + "<title>Sending the LTI message back...</title>\n</head>\n"
            + "<body onload=\"document.forms[0].submit();\">\n"
            + "<form method=\"post\" action=\"" + action + "\">\n"
            + "<input type=\"hidden\" name=\"" + firstField + "\" value=\"" + firstValue + "\"/>\n"
            + "<input type=\"hidden\" name=\"" + secondField + "\" value=\"" + secondValue + "\"/>\n"
            + "</form>\n</body>\n</html>";
    }

    private static Map<String, String> queryParameters(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        Map<String, String> parameters = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return parameters;
        }
        for (String pair : rawQuery.split("&")) {
            int separator = pair.indexOf('=');
            if (separator > 0) {
                String name = URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8);
                parameters.put(name, value);
            }
        }
        return parameters;
    }

    private static String formField(String body, String field) {
        return Arrays.stream(body.split("&"))
            .filter(pair -> pair.startsWith(field + "="))
            .map(pair -> URLDecoder.decode(pair.substring(field.length() + 1), StandardCharsets.UTF_8))
            .collect(Collectors.joining(""));
    }

    private static void respond(HttpExchange exchange, int status, String contentType, String body)
        throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }

    private record PendingLaunch(Map<String, Object> claims) {
        private PendingLaunch(Map<String, Object> claims) {
            this.claims = java.util.Collections.unmodifiableMap(claims);
        }
    }
}
