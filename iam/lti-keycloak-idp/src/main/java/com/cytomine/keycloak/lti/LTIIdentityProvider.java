package com.cytomine.keycloak.lti;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.events.EventBuilder;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.JsonSerialization;
import org.keycloak.models.FederatedIdentityModel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Speaks the LTI 1.3 "platform-initiated" launch dialect and presents it to
 * the rest of Keycloak as a normal broker.
 *
 * Two moments matter:
 *  - performLogin(): called once Keycloak has decided "the user should log
 *    in via this IdP" (i.e. kc_idp_hint routed here from the login-init
 *    endpoint). Redirects the browser to the platform's authorization
 *    endpoint per the OIDC third-party-initiated-login flow LTI 1.3 uses.
 *  - the callback endpoint below: receives the platform's POSTed id_token,
 *    verifies it, and turns it into a BrokeredIdentityContext, which is what
 *    Keycloak's normal first-broker-login flow (create/link user, issue
 *    tokens) then takes over from.
 */
public class LTIIdentityProvider extends AbstractIdentityProvider<LTIIdentityProviderConfig> {

    private static final Logger log = Logger.getLogger(LTIIdentityProvider.class);

    // LTI claim URIs (IMS Global / 1EdTech LTI 1.3 Core spec)
    private static final String CLAIM_MESSAGE_TYPE = "https://purl.imsglobal.org/spec/lti/claim/message_type";
    private static final String CLAIM_VERSION = "https://purl.imsglobal.org/spec/lti/claim/version";
    private static final String CLAIM_DEPLOYMENT_ID = "https://purl.imsglobal.org/spec/lti/claim/deployment_id";
    private static final String CLAIM_TARGET_LINK_URI = "https://purl.imsglobal.org/spec/lti/claim/target_link_uri";
    private static final String CLAIM_ROLES = "https://purl.imsglobal.org/spec/lti/claim/roles";
    private static final String CLAIM_CONTEXT = "https://purl.imsglobal.org/spec/lti/claim/context";

    // Client notes used to shuttle data from login-init through to callback
    static final String NOTE_TARGET_LINK_URI = "LTI_TARGET_LINK_URI";
    static final String NOTE_NONCE = "LTI_NONCE";

    private final LTIJwtValidator jwtValidator = new LTIJwtValidator();

    public LTIIdentityProvider(KeycloakSession session, LTIIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).build();
    }

    // ---------------------------------------------------------------
    // Step A: redirect the browser to the LMS's authorization endpoint
    // ---------------------------------------------------------------

    @Override
    public Response performLogin(AuthenticationRequest request) {
        try {
            AuthenticationSessionModel authSession = request.getAuthenticationSession();

            // The login-init resource packed {login_hint, lti_message_hint,
            // target_link_uri, deployment_id} into the standard "login_hint"
            // query param it sent to /protocol/openid-connect/auth - Keycloak
            // stores that verbatim under this client note. See LTILaunchHint.
            String packed = authSession.getClientNote(org.keycloak.protocol.oidc.OIDCLoginProtocol.LOGIN_HINT_PARAM);
            if (packed == null) {
                throw new IdentityBrokerException(
                        "Missing LTI launch hint - this IdP must be entered via the /lti/login-init endpoint, not directly.");
            }
            Map<String, String> hint = LTILaunchHint.decode(packed);
            String loginHint = hint.get("login_hint");
            String messageHint = hint.get("lti_message_hint");
            String targetLinkUri = hint.get("target_link_uri");
            if (targetLinkUri != null) {
                authSession.setClientNote(NOTE_TARGET_LINK_URI, targetLinkUri);
            }

            String nonce = UUID.randomUUID().toString();
            authSession.setClientNote(NOTE_NONCE, nonce);

            String redirectUri = request.getRedirectUri(); // Keycloak's broker callback URL for this IdP

            UriBuilder uriBuilder = UriBuilder.fromUri(getConfig().getPlatformAuthorizationEndpoint())
                    .queryParam("scope", "openid")
                    .queryParam("response_type", "id_token")
                    .queryParam("response_mode", "form_post")
                    .queryParam("prompt", "none")
                    .queryParam("client_id", getConfig().getToolClientId())
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("login_hint", loginHint)
                    .queryParam("state", authSession.getParentSession().getId())
                    .queryParam("nonce", nonce);

            if (messageHint != null) {
                uriBuilder.queryParam("lti_message_hint", messageHint);
            }

            return Response.seeOther(uriBuilder.build()).build();
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not build LTI launch redirect", e);
        }
    }

    // ---------------------------------------------------------------
    // Step B: receive + validate the id_token the LMS posts back
    // ---------------------------------------------------------------

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(this, realm, callback, event);
    }

    public static class Endpoint {
        private final RealmModel realm;
        private final AuthenticationCallback callback;
        private final EventBuilder event;
        private final LTIIdentityProvider provider;

        public Endpoint(LTIIdentityProvider provider, RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
            this.realm = realm;
            this.callback = callback;
            this.event = event;
            this.provider = provider;
        }

        @POST
        @jakarta.ws.rs.Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response handleLaunchResponse(@FormParam("id_token") String idToken,
                                              @FormParam("state") String state,
                                              @FormParam("error") String error) {
            if (error != null) {
                log.warnf("LTI platform returned error on launch: %s", error);
                return callback.error("lti_platform_error");
            }
            if (idToken == null) {
                return callback.error("missing_id_token");
            }

            try {
                BrokeredIdentityContext identity = provider.validateAndExtract(idToken);
                identity.setIdpConfig(provider.getConfig());
                identity.setIdp(provider);
                return callback.authenticated(identity);
            } catch (Exception e) {
                log.warn("LTI launch validation failed", e);
                return callback.error("invalid_lti_launch");
            }
        }
    }

    // ---------------------------------------------------------------
    // Validation + claim extraction
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    BrokeredIdentityContext validateAndExtract(String rawIdToken) throws Exception {
        JWSInput jws = jwtValidator.verify(rawIdToken, getConfig().getPlatformJwksUrl());
        Map<String, Object> claims = JsonSerialization.readValue(jws.readContentAsString(), Map.class);

        // --- Required LTI / OIDC claim checks -----------------------------------
        String iss = (String) claims.get("iss");
        if (!getConfig().getPlatformIssuer().equals(iss)) {
            throw new IllegalArgumentException("iss mismatch: expected " + getConfig().getPlatformIssuer() + " got " + iss);
        }

        Object aud = claims.get("aud");
        boolean audOk = (aud instanceof String s && s.equals(getConfig().getToolClientId()))
                || (aud instanceof List<?> l && l.contains(getConfig().getToolClientId()));
        if (!audOk) {
            throw new IllegalArgumentException("aud does not include tool client_id");
        }

        String messageType = (String) claims.get(CLAIM_MESSAGE_TYPE);
        if (!"LtiResourceLinkRequest".equals(messageType)) {
            // Deep linking / other LTI message types would need their own handling;
            // this scaffold only implements the plain resource-link launch.
            throw new IllegalArgumentException("Unsupported LTI message_type: " + messageType);
        }

        if (!"1.3.0".equals(claims.get(CLAIM_VERSION))) {
            throw new IllegalArgumentException("Unsupported LTI version: " + claims.get(CLAIM_VERSION));
        }

        String deploymentId = (String) claims.get(CLAIM_DEPLOYMENT_ID);
        List<String> allowed = List.of(getConfig().getAllowedDeploymentIds().split("\\s*,\\s*"));
        if (!allowed.contains(deploymentId)) {
            throw new IllegalArgumentException("deployment_id not allowed: " + deploymentId);
        }

        // NOTE: nonce and exp/iat/state cross-checks against the values stashed in
        // NOTE_NONCE / the auth session are omitted from this scaffold for brevity -
        // add them here (reject on mismatch or expiry) before using this in production.

        String subject = (String) claims.get(getConfig().getSubjectClaim());
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Missing subject claim");
        }

        BrokeredIdentityContext identity = new BrokeredIdentityContext(subject);
        identity.setIdpConfig(getConfig());

        identity.setUsername(subject);
        if (claims.get("email") != null) {
            identity.setEmail((String) claims.get("email"));
        }
        if (claims.get("given_name") != null) {
            identity.setFirstName((String) claims.get("given_name"));
        }
        if (claims.get("family_name") != null) {
            identity.setLastName((String) claims.get("family_name"));
        }

        // Stash the raw claims so IdentityProviderMappers can pull LTI roles,
        // course/context info, etc. into Keycloak roles or user attributes.
        identity.getContextData().put("LTI_CLAIMS", claims);
        identity.getContextData().put("LTI_ROLES", claims.get(CLAIM_ROLES));
        identity.getContextData().put("LTI_CONTEXT", claims.get(CLAIM_CONTEXT));
        identity.getContextData().put("LTI_TARGET_LINK_URI", claims.get(CLAIM_TARGET_LINK_URI));

        return identity;
    }
}
