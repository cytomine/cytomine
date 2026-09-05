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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Identity provider implementation for LTI 1.3 platform-initiated launches.
 */
public class LTIIdentityProvider extends AbstractIdentityProvider<LTIIdentityProviderConfig> {

    private static final Logger log = Logger.getLogger(LTIIdentityProvider.class);

    // LTI 1.3 claim URIs
    private static final String CLAIM_MESSAGE_TYPE = "https://purl.imsglobal.org/spec/lti/claim/message_type";
    private static final String CLAIM_VERSION = "https://purl.imsglobal.org/spec/lti/claim/version";
    private static final String CLAIM_DEPLOYMENT_ID = "https://purl.imsglobal.org/spec/lti/claim/deployment_id";
    private static final String CLAIM_TARGET_LINK_URI = "https://purl.imsglobal.org/spec/lti/claim/target_link_uri";
    private static final String CLAIM_ROLES = "https://purl.imsglobal.org/spec/lti/claim/roles";
    private static final String CLAIM_CONTEXT = "https://purl.imsglobal.org/spec/lti/claim/context";

    // Authentication session notes
    static final String NOTE_TARGET_LINK_URI = "LTI_TARGET_LINK_URI";
    static final String NOTE_NONCE = "LTI_NONCE";

    // Clock-skew tolerance applied to both directions of the exp/iat window.
    private static final long CLOCK_SKEW_LEEWAY_SECONDS = 60;
    // Reject launches whose id_token was issued (iat) further in the past than this,
    // independent of exp - keeps the acceptance window tight even if a platform
    // sets an unusually long exp. 5 minutes matches common LTI/OIDC guidance.
    private static final long MAX_IAT_AGE_SECONDS = 300;

    private final LTIJwtValidator jwtValidator = new LTIJwtValidator();

    public LTIIdentityProvider(KeycloakSession session, LTIIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).build();
    }

    // Step A: redirect browser to LMS authorization endpoint
    @Override
    public Response performLogin(AuthenticationRequest request) {
        try {
            AuthenticationSessionModel authSession = request.getAuthenticationSession();

            // Retrieve encoded launch hints passed via login_hint param
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

            String redirectUri = request.getRedirectUri();

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

    // Step B: receive and validate launch id_token
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

            // Same access pattern as LTI11IdentityProvider.Endpoint (provider.session ...):
            // the browser's cookie/redirect already ties this callback back to the exact
            // authentication session that performLogin() started, which is what lets us
            // compare the nonce/state we generated then against what came back now.
            AuthenticationSessionModel authSession = provider.session.getContext().getAuthenticationSession();
            if (authSession == null) {
                log.warn("LTI launch callback received with no active authentication session");
                return callback.error("missing_authentication_session");
            }

            try {
                BrokeredIdentityContext identity = provider.validateAndExtract(idToken, state, authSession);
                identity.setIdpConfig(provider.getConfig());
                identity.setIdp(provider);
                return callback.authenticated(identity);
            } catch (Exception e) {
                log.warn("LTI launch validation failed", e);
                return callback.error("invalid_lti_launch");
            }
        }
    }

    // Validation and claim extraction
    @SuppressWarnings("unchecked")
    BrokeredIdentityContext validateAndExtract(String rawIdToken, String returnedState,
                                               AuthenticationSessionModel authSession) throws Exception {
        JWSInput jws = jwtValidator.verify(rawIdToken, getConfig().getPlatformJwksUrl());
        Map<String, Object> claims = JsonSerialization.readValue(jws.readContentAsString(), Map.class);

        // Required LTI / OIDC claim checks
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

        // --- state: binds this response back to the browser session that started the
        // launch (CSRF / session-fixation protection). We set this to the parent
        // session's id in performLogin() and expect the platform to echo it back verbatim.
        String expectedState = authSession.getParentSession().getId();
        if (expectedState == null || !expectedState.equals(returnedState)) {
            throw new IllegalArgumentException("state mismatch - possible CSRF or a stale/replayed launch response");
        }

        // --- nonce: must match the single-use value we generated and sent in
        // performLogin(). Consuming it immediately (removeClientNote) means even a
        // resubmission of the exact same id_token against this same session is rejected.
        String expectedNonce = authSession.getClientNote(NOTE_NONCE);
        Object nonceClaim = claims.get("nonce");
        if (expectedNonce == null) {
            throw new IllegalArgumentException("No nonce recorded for this authentication session - was performLogin() skipped?");
        }
        if (!(nonceClaim instanceof String) || !expectedNonce.equals(nonceClaim)) {
            throw new IllegalArgumentException("nonce mismatch - possible replay of a previous launch");
        }
        authSession.removeClientNote(NOTE_NONCE);

        // --- exp / iat: reject expired tokens and tokens issued implausibly far in
        // the past or future, independent of whatever exp the platform set.
        long now = Instant.now().getEpochSecond();

        Number expClaim = (Number) claims.get("exp");
        if (expClaim == null) {
            throw new IllegalArgumentException("Missing exp claim");
        }
        if (now > expClaim.longValue() + CLOCK_SKEW_LEEWAY_SECONDS) {
            throw new IllegalArgumentException("Launch id_token has expired");
        }

        Number iatClaim = (Number) claims.get("iat");
        if (iatClaim == null) {
            throw new IllegalArgumentException("Missing iat claim");
        }
        if (iatClaim.longValue() > now + CLOCK_SKEW_LEEWAY_SECONDS) {
            throw new IllegalArgumentException("Launch id_token iat is in the future");
        }
        if (now - iatClaim.longValue() > MAX_IAT_AGE_SECONDS) {
            throw new IllegalArgumentException("Launch id_token is too old (iat too far in the past)");
        }

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

        // Attach claims for identity provider mappers
        identity.getContextData().put("LTI_CLAIMS", claims);
        identity.getContextData().put("LTI_ROLES", claims.get(CLAIM_ROLES));
        identity.getContextData().put("LTI_CONTEXT", claims.get(CLAIM_CONTEXT));
        identity.getContextData().put("LTI_TARGET_LINK_URI", claims.get(CLAIM_TARGET_LINK_URI));

        return identity;
    }
}