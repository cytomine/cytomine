package org.cytomine.e2etests.lti;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * One recorded LMS deep-linking registration (login-initiation parameters plus the shape of the
 * {@code LtiDeepLinkingRequest} id_token the LMS would send). Fixtures live in
 * {@code src/test/resources/lti/} and are loaded by {@link LtiFixtureLoader}.
 */
final class LtiFixture {

    /** Login initiation parameters as the LMS sends them (method, iss, client_id, hints...). */
    private final Map<String, String> loginInitParameters;
    /** Body claims of the platform's {@code LtiDeepLinkingRequest} id_token (without state/nonce). */
    private final Map<String, Object> requestClaims;

    LtiFixture(Map<String, String> loginInitParameters, Map<String, Object> requestClaims) {
        this.loginInitParameters = new LinkedHashMap<>(loginInitParameters);
        this.requestClaims = new LinkedHashMap<>(requestClaims);
    }

    String method() {
        return loginInitParameters.getOrDefault("method", "POST");
    }

    String loginInitParameter(String name) {
        return loginInitParameters.get(name);
    }

    String alias() {
        return loginInitParameters.get("alias");
    }

    String platformIssuer() {
        return loginInitParameters.get("iss");
    }

    String toolClientId() {
        return loginInitParameters.get("client_id");
    }

    Map<String, String> loginInitParameters() {
        return new LinkedHashMap<>(loginInitParameters);
    }

    /**
     * Deep-copies the request claims so each test can apply its own variant without touching the
     * shared fixture (e.g. removing {@code data} or the presentation-target list).
     */
    Map<String, Object> copyRequestClaims() {
        return deepCopy(requestClaims);
    }

    private static Map<String, Object> deepCopy(Map<String, Object> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    private static Object deepCopyValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            for (Object item : list) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        return value;
    }

    /**
     * Loads a fixture JSON from the classpath ({@code lti/<id>-deep-linking-request.json}).
     * The optional {@code alias} key names the Keycloak identity provider alias to use.
     */
    static LtiFixture load(String id) {
        String path = "lti/" + id + "-deep-linking-request.json";
        try (InputStream in = LtiFixture.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing LTI fixture resource: " + path);
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                .replace("{{simulator}}", LtiSettings.simulatorBase());
            JSONObject root = (JSONObject) JSONValue.parse(json);
            if (root == null) {
                throw new IllegalStateException("LTI fixture is not valid JSON: " + path);
            }
            Map<String, String> loginInit = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : ((JSONObject) root.get("loginInit")).entrySet()) {
                loginInit.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            Map<String, Object> claims = (JSONObject) root.get("claims");
            return new LtiFixture(loginInit, claims);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read LTI fixture: " + path, e);
        }
    }

    /** Deep-linking settings map of a fixture's request claims (may be adjusted by variants). */
    @SuppressWarnings("unchecked")
    static Map<String, Object> deepLinkingSettings(Map<String, Object> claims) {
        Object settings = claims.get("https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings");
        if (settings instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalStateException("Fixture claims do not contain deep_linking_settings");
    }

    /** Removes a top-level claim from a claims copy (for "platform did not send X" variants). */
    static void removeClaim(Map<String, Object> claims, String name) {
        claims.remove(name);
    }

    static void removeFromDeepLinkingSettings(Map<String, Object> claims, String key) {
        deepLinkingSettings(claims).remove(key);
    }

    static void setPresentationTargets(Map<String, Object> claims, List<String> targets) {
        deepLinkingSettings(claims).put("accept_presentation_document_targets", targets);
    }
}
