package com.cytomine.keycloak.lti;
import org.keycloak.util.JsonSerialization;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

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
