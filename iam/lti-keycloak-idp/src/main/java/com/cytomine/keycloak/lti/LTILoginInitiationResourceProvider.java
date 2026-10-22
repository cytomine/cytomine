package com.cytomine.keycloak.lti;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jws.Algorithm;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.util.JsonSerialization;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LTILoginInitiationResourceProvider implements RealmResourceProvider {

    private static final Logger log = Logger.getLogger(LTILoginInitiationResourceProvider.class);

    private static final String RESPONSE_JWT_TTL_SECONDS = "300";
    private static final String CLAIM_MESSAGE_TYPE = "https://purl.imsglobal.org/spec/lti/claim/message_type";
    private static final String CLAIM_VERSION = "https://purl.imsglobal.org/spec/lti/claim/version";
    private static final String CLAIM_DEPLOYMENT_ID = "https://purl.imsglobal.org/spec/lti/claim/deployment_id";
    private static final String CLAIM_CONTENT_ITEMS = "https://purl.imsglobal.org/spec/lti-dl/claim/content_items";
    private static final String CLAIM_DEEP_LINKING_SETTINGS =
        "https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings";

    private final KeycloakSession session;

    public LTILoginInitiationResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
        // no-op
    }

    /**
     * Moodle (and most other platforms) POST the OIDC third-party-initiated login to the
     * login initiation URL with the LTI parameters urlencoded in the request body.
     */
    @POST
    @Path("login-init")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response loginInit(@FormParam("iss") String iss,
                               @FormParam("login_hint") String loginHint,
                               @FormParam("target_link_uri") String targetLinkUri,
                               @FormParam("client_id") String clientId,
                               @FormParam("lti_deployment_id") String deploymentId,
                               @FormParam("lti_message_hint") String messageHint) {
        return doLoginInit(iss, loginHint, targetLinkUri, clientId, deploymentId, messageHint);
    }

    /**
     * Blackboard (and some other platforms) GET the login initiation URL with the LTI
     * parameters urlencoded in the query string. JAX-RS decodes them automatically, so the
     * handling is identical to the POST variant.
     */
    @GET
    @Path("login-init")
    public Response loginInitGet(@QueryParam("iss") String iss,
                                 @QueryParam("login_hint") String loginHint,
                                 @QueryParam("target_link_uri") String targetLinkUri,
                                 @QueryParam("client_id") String clientId,
                                 @QueryParam("lti_deployment_id") String deploymentId,
                                 @QueryParam("lti_message_hint") String messageHint) {
        return doLoginInit(iss, loginHint, targetLinkUri, clientId, deploymentId, messageHint);
    }

    private Response doLoginInit(String iss,
                                 String loginHint,
                                 String targetLinkUri,
                                 String clientId,
                                 String deploymentId,
                                 String messageHint) {

        if (iss == null || loginHint == null || targetLinkUri == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required LTI login initiation parameters (iss, login_hint, target_link_uri)")
                    .build();
        }

        RealmModel realm = session.getContext().getRealm();

        Optional<IdentityProviderModel> match = realm.getIdentityProvidersStream()
                .filter(idp -> LTIIdentityProviderFactory.PROVIDER_ID.equals(idp.getProviderId()))
                .filter(idp -> iss.equals(idp.getConfig().get("platformIssuer")))
                .filter(idp -> clientId == null || clientId.equals(idp.getConfig().get("toolClientId")))
                .findFirst();

        if (match.isEmpty()) {
            log.warnf("LTI login-init: no configured IdP matches iss=%s client_id=%s", iss, clientId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No LTI identity provider is configured for this platform/client_id")
                    .build();
        }

        IdentityProviderModel idp = match.get();
        String keycloakClientId = idp.getConfig().get("keycloakClientId");
        if (keycloakClientId == null) {
            return Response.serverError()
                    .entity("Matched LTI identity provider is missing its keycloakClientId configuration")
                    .build();
        }

        String toolRedirectUri = targetLinkUri;

        String packedHint = LTILaunchHint.encode(loginHint, messageHint, targetLinkUri, deploymentId);

        UriBuilder authUrl = UriBuilder.fromUri(session.getContext().getUri().getBaseUri())
                .path("realms").path(realm.getName())
                .path("protocol/openid-connect/auth")
                .queryParam("client_id", keycloakClientId)
                .queryParam("redirect_uri", toolRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("kc_idp_hint", idp.getAlias())
                // Force Keycloak to run the identity broker for every LTI launch. Without this, an
                // existing Keycloak SSO session satisfies the browser flow's Cookie authenticator and
                // the IdP (and therefore our broker callback - where deep-linking launches are detected
                // and turned into a content-selection redirect) is skipped entirely.
                .queryParam("prompt", "login")
                .queryParam("login_hint", packedHint);

        return Response.seeOther(authUrl.build()).build();
    }

    /**
     * Receives a selected content item from the Cytomine Web-UI picker (deep linking), builds and signs the
     * {@code LtiDeepLinkingResponse} JWT, and answers with an auto-submitting form that posts it back to the
     * platform's {@code deep_link_return_url}. Implemented as a form POST so the picker can submit it straight
     * from the browser without CORS preflight.
     *
     * Security: the {@code ctx} token is a high-entropy, single-use, short-lived reference to the return context
     * captured from the validated launch. It is consumed on read, so a selection can only be returned once.
     */
    @POST
    @Path("dl-return")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deepLinkReturn(@FormParam("ctx") String ctxToken,
                                   @FormParam("url") String url,
                                   @FormParam("title") String title,
                                   @FormParam("text") String text,
                                   @FormParam("custom") String customJson) {
        if (ctxToken == null || ctxToken.isBlank() || url == null || url.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required deep-linking return parameters (ctx, url)")
                    .build();
        }

        SingleUseObjectProvider singleUse = session.singleUseObjects();
        Map<String, String> returnCtx =
                singleUse.remove(LTIIdentityProvider.DL_CTX_SINGLE_USE_PREFIX + ctxToken.trim());
        if (returnCtx == null) {
            log.warnf("LTI dl-return: unknown, expired or already-used ctx token");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Unknown, expired or already-used deep-linking context token. " +
                            "Start the content selection again from your LMS.")
                    .build();
        }

        String returnUrl = returnCtx.get(LTIIdentityProvider.DL_CTX_RETURN_URL);

        try {
            Map<String, String> custom = sanitizeCustom(parseCustom(customJson));
            String jwt = buildDeepLinkingResponse(returnCtx, url, title, text, custom);
            log.infof("LTI dl-return: selection accepted, posting LtiDeepLinkingResponse to %s", returnUrl);
            return Response.ok(autoSubmitForm(returnUrl, jwt), MediaType.TEXT_HTML_TYPE).build();
        } catch (Exception e) {
            log.warnf("LTI dl-return: failed to build the LtiDeepLinkingResponse", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to build the LTI deep-linking response: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Builds and signs the tool's LtiDeepLinkingResponse JWT for the platform. The returned token verifies
     * against the tool's public JWKS (which Moodle holds in the tool type's "Public keyset" field), so we sign
     * with the realm's active RSA signing key and set the matching kid.
     */
    private String buildDeepLinkingResponse(Map<String, String> returnCtx, String url, String title, String text,
                                            Map<String, String> custom) {
        RealmModel realm = session.getContext().getRealm();
        KeyWrapper signingKey = session.keys().getActiveKey(realm, KeyUse.SIG, "RS256");
        if (signingKey == null || signingKey.getPrivateKey() == null) {
            throw new IllegalStateException(
                    "No active realm RSA-256 signing key available to sign the LtiDeepLinkingResponse");
        }

        Map<String, Object> contentItem = new LinkedHashMap<>();
        contentItem.put("type", "ltiResourceLink");
        if (title != null && !title.isBlank()) {
            contentItem.put("title", title);
        }
        if (text != null && !text.isBlank()) {
            contentItem.put("text", text);
        }
        contentItem.put("url", url);
        if (!custom.isEmpty()) {
            contentItem.put("custom", custom);
        }

        // Echo the platform's opaque `data` value back so Moodle can correlate the response with the request.
        Map<String, Object> deepLinkingSettings = new LinkedHashMap<>();
        if (returnCtx.get(LTIIdentityProvider.DL_CTX_DATA) != null) {
            deepLinkingSettings.put("data", returnCtx.get(LTIIdentityProvider.DL_CTX_DATA));
        }

        long now = Instant.now().getEpochSecond();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", returnCtx.get(LTIIdentityProvider.DL_CTX_CLIENT_ID));
        payload.put("aud", returnCtx.get(LTIIdentityProvider.DL_CTX_ISS));
        payload.put("sub", returnCtx.get(LTIIdentityProvider.DL_CTX_SUB));
        payload.put("exp", now + Long.parseLong(RESPONSE_JWT_TTL_SECONDS));
        payload.put("iat", now);
        payload.put("nonce", UUID.randomUUID().toString());
        payload.put(CLAIM_DEPLOYMENT_ID, returnCtx.get(LTIIdentityProvider.DL_CTX_DEPLOYMENT_ID));
        payload.put(CLAIM_MESSAGE_TYPE, "LtiDeepLinkingResponse");
        payload.put(CLAIM_VERSION, "1.3.0");
        payload.put(CLAIM_CONTENT_ITEMS, List.of(contentItem));
        payload.put(CLAIM_DEEP_LINKING_SETTINGS, deepLinkingSettings);

        try {
            return new JWSBuilder().kid(signingKey.getKid())
                    .jsonContent(payload)
                    .sign(Algorithm.RS256, (PrivateKey) signingKey.getPrivateKey());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign the LtiDeepLinkingResponse JWT", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseCustom(String customJson) {
        Map<String, Object> custom = new LinkedHashMap<>();
        if (customJson == null || customJson.isBlank()) {
            return custom;
        }
        try {
            Object parsed = JsonSerialization.readValue(customJson, Object.class);
            if (parsed instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof String key && entry.getValue() != null) {
                        custom.put(key, entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warnf("LTI dl-return: could not parse custom parameters JSON - ignoring them: %s", e.getMessage());
        }
        return custom;
    }

    private Map<String, String> sanitizeCustom(Map<String, Object> custom) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : custom.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
                out.put(entry.getKey(), String.valueOf(value));
            } else {
                log.warnf("LTI dl-return: dropping non-scalar custom parameter '%s' (value was %s)",
                        entry.getKey(), value.getClass().getSimpleName());
            }
        }
        return out;
    }

    private String autoSubmitForm(String returnUrl, String jwt) {
        return "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n" +
                "<title>Returning your selection to the LMS...</title>\n</head>\n" +
                "<body onload=\"document.forms[0].submit();\">\n" +
                "<form method=\"post\" action=\"" + escapeHtml(returnUrl) + "\" " +
                "enctype=\"application/x-www-form-urlencoded\">\n" +
                "<input type=\"hidden\" name=\"JWT\" value=\"" + escapeHtml(jwt) + "\"/>\n" +
                "<noscript>\n<p>Content selected. If you are not redirected automatically, " +
                "<button type=\"submit\">click here to return to your LMS</button>.</p>\n</noscript>\n" +
                "</form>\n</body>\n</html>";
    }

    private String escapeHtml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
