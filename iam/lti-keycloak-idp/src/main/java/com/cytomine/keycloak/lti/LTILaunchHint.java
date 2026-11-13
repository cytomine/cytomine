package com.cytomine.keycloak.lti;
import org.keycloak.util.JsonSerialization;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The launch data captured by /lti/login-init: the platform's login_hint, lti_message_hint,
 * target_link_uri and deployment ID. It is stored in Keycloak's single-use object store (an opaque,
 * short-lived key travels in login_hint instead, see LTIIdentityProvider#performLogin); the base64
 * JSON encoding below is only kept as the legacy fallback format for authentication sessions that
 * still carry the old packed hint.
 */
public final class LTILaunchHint {

    private LTILaunchHint() {}

    /**
     * Structured launch data, also the persisted form in the single-use object store.
     */
    public static Map<String, String> data(String loginHint, String messageHint, String targetLinkUri,
                                           String deploymentId) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("login_hint", loginHint);
        if (messageHint != null) map.put("lti_message_hint", messageHint);
        if (targetLinkUri != null) map.put("target_link_uri", targetLinkUri);
        if (deploymentId != null) map.put("deployment_id", deploymentId);
        return map;
    }

    /**
     * Legacy packed form: unpadded base64 JSON, superseded by the single-use store mechanism.
     */
    public static String encode(String loginHint, String messageHint, String targetLinkUri, String deploymentId) {
        try {
            String json = JsonSerialization.writeValueAsString(
                    data(loginHint, messageHint, targetLinkUri, deploymentId));
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
