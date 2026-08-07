package com.cytomine.keycloak.lti;
import org.keycloak.util.JsonSerialization;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The LTI login-initiation request and the OIDC-style auth request that
 * follows it don't share a session yet, so we can't stash values in
 * server-side state between the two. Instead we pack the handful of values
 * we need (the real LTI login_hint, the optional lti_message_hint, and
 * target_link_uri) into a single JSON blob and carry it through as the
 * standard "login_hint" query parameter on /protocol/openid-connect/auth -
 * Keycloak stores that verbatim as a client note we can read back in
 * performLogin().
 *
 * This is a pragmatic shortcut, not a security boundary: nothing sensitive
 * goes in here (these are all values the browser/LMS already sees), and the
 * real trust decision still happens when the platform's signed id_token
 * comes back and is verified against its JWKS.
 */
public final class LTILaunchHint {

    private LTILaunchHint() {}

    public static String encode(String loginHint, String messageHint, String targetLinkUri, String deploymentId) {
        try {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("login_hint", loginHint);
            if (messageHint != null) map.put("lti_message_hint", messageHint);
            if (targetLinkUri != null) map.put("target_link_uri", targetLinkUri);
            if (deploymentId != null) map.put("deployment_id", deploymentId);
            String json = JsonSerialization.writeValueAsString(map);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode LTI launch hint", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> decode(String packed) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(packed);
            return JsonSerialization.readValue(new String(json, StandardCharsets.UTF_8), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode LTI launch hint - was this IdP entered directly instead of via /lti/login-init?", e);
        }
    }
}
