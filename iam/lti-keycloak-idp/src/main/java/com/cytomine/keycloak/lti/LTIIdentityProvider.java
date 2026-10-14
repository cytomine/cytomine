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
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.util.Map;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LTIIdentityProvider extends AbstractIdentityProvider<LTIIdentityProviderConfig> {

    private static final Logger log = Logger.getLogger(LTIIdentityProvider.class);

    // LTI 1.3 claim URIs
    private static final String CLAIM_MESSAGE_TYPE = "https://purl.imsglobal.org/spec/lti/claim/message_type";
    private static final String CLAIM_VERSION = "https://purl.imsglobal.org/spec/lti/claim/version";
    private static final String CLAIM_DEPLOYMENT_ID = "https://purl.imsglobal.org/spec/lti/claim/deployment_id";
    private static final String CLAIM_TARGET_LINK_URI = "https://purl.imsglobal.org/spec/lti/claim/target_link_uri";
    private static final String CLAIM_ROLES = "https://purl.imsglobal.org/spec/lti/claim/roles";
    private static final String CLAIM_CONTEXT = "https://purl.imsglobal.org/spec/lti/claim/context";
    private static final String CLAIM_LIS = "https://purl.imsglobal.org/spec/lti/claim/lis";
    private static final String CLAIM_CUSTOM = "https://purl.imsglobal.org/spec/lti/claim/custom";
    private static final String CLAIM_EXT = "https://purl.imsglobal.org/spec/lti/claim/ext";
    // Deep-linking (Content-Item Message) claims live under the lti-dl namespace.
    private static final String CLAIM_DEEP_LINKING_SETTINGS = "https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings";

    // Authentication session notes
    static final String NOTE_TARGET_LINK_URI = "LTI_TARGET_LINK_URI";
    // Pending nonces are stored as a comma-separated set. A single nonce slot is not enough:
    // overlapping launches (e.g. the LMS popup was re-opened before the previous id_token POST
    // arrived) share the same root authentication session, and overwriting the note would make a
    // legitimately late id_token look like a replay.
    static final String NOTE_NONCE = "LTI_NONCE";
    static final int MAX_PENDING_NONCES = 16;

    // Context-data keys attached to the BrokeredIdentityContext for identity provider mappers.
    static final String CTX_LTI_CLAIMS = "LTI_CLAIMS";
    static final String CTX_LTI_ROLES = "LTI_ROLES";
    static final String CTX_LTI_CONTEXT = "LTI_CONTEXT";
    static final String CTX_LTI_TARGET_LINK_URI = "LTI_TARGET_LINK_URI";
    static final String CTX_LTI_CUSTOM = "LTI_CUSTOM";
    static final String CTX_DEEP_LINKING_SETTINGS = "LTI_DEEP_LINKING_SETTINGS";
    static final String CTX_DEEP_LINKING_DATA = "LTI_DEEP_LINKING_DATA";

    // Deep-linking return context, stashed in Keycloak's single-use object store so the
    // Web-UI picker can later hand the selection back to the local /lti/dl-return endpoint.
    static final String DL_CTX_SINGLE_USE_PREFIX = "lti-dl:";
    static final long DL_CTX_TTL_MS = 10 * 60 * 1000L; // 10 minutes
    static final String DL_CTX_RETURN_URL = "returnUrl";
    static final String DL_CTX_DATA = "data";
    static final String DL_CTX_ISS = "iss";
    static final String DL_CTX_CLIENT_ID = "clientId";
    static final String DL_CTX_DEPLOYMENT_ID = "deploymentId";
    static final String DL_CTX_SUB = "sub";

    // Clock-skew tolerance applied to both directions of the exp/iat window.
    private static final long CLOCK_SKEW_LEEWAY_SECONDS = 60;
    private static final long MAX_IAT_AGE_SECONDS = 300;

    public LTIIdentityProvider(KeycloakSession session, LTIIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).build();
    }

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
            appendPendingNonce(authSession, nonce);

            String redirectUri = request.getRedirectUri();
            String encodedState = request.getState().getEncoded();

            UriBuilder uriBuilder = UriBuilder.fromUri(getConfig().getPlatformAuthorizationEndpoint())
                .queryParam("scope", "openid")
                .queryParam("response_type", "id_token")
                .queryParam("response_mode", "form_post")
                .queryParam("prompt", "none")
                .queryParam("client_id", getConfig().getToolClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("login_hint", loginHint)
                .queryParam("state", encodedState)
                .queryParam("nonce", nonce);

            if (messageHint != null) {
                String safeMessageHint = messageHint.replace("{", "%7B").replace("}", "%7D");
                uriBuilder.queryParam("lti_message_hint", safeMessageHint);
            }

            return Response.seeOther(uriBuilder.build()).build();
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not build LTI launch redirect", e);
        }
    }

    /**
     * Records a freshly generated nonce for this launch. Nonces accumulate instead of overwriting so
     * that overlapping launches sharing one authentication session do not invalidate each other: each
     * launch appends its own nonce, and whichever id_token POST arrives first with a matching nonce is
     * accepted. A bounded FIFO set keeps the note from growing without limit.
     */
    static void appendPendingNonce(AuthenticationSessionModel authSession, String nonce) {
        String current = authSession.getClientNote(NOTE_NONCE);
        Set<String> pending = current == null || current.isBlank()
            ? new LinkedHashSet<>()
            : new LinkedHashSet<>(Arrays.asList(current.split(",")));
        pending.add(nonce);
        Iterator<String> it = pending.iterator();
        while (pending.size() > MAX_PENDING_NONCES && it.hasNext()) {
            it.next();
            it.remove();
        }
        authSession.setClientNote(NOTE_NONCE, String.join(",", pending));
    }

    /**
     * Removes one pending nonce matching the launch's id_token. Returns {@code true} only when the
     * nonce was actually listed, preserving one-shot replay protection per nonce.
     */
    static boolean consumePendingNonce(AuthenticationSessionModel authSession, String nonce) {
        String current = authSession.getClientNote(NOTE_NONCE);
        if (current == null || current.isBlank()) {
            return false;
        }
        Set<String> pending = new LinkedHashSet<>(Arrays.asList(current.split(",")));
        if (!pending.remove(nonce)) {
            return false;
        }
        if (pending.isEmpty()) {
            authSession.removeClientNote(NOTE_NONCE);
        } else {
            authSession.setClientNote(NOTE_NONCE, String.join(",", pending));
        }
        return true;
    }

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

            AuthenticationSessionManager asm = new AuthenticationSessionManager(provider.session);
            RootAuthenticationSessionModel rootAuthSession = asm.getCurrentRootAuthenticationSession(realm);

            // Collect candidate authentication sessions under the root session. The launch can arrive
            // for any session that was restarted from the browser's session cookie; overlapping or
            // re-opened popups can leave several client sessions under the same root session. Prefer
            // sessions belonging to the Keycloak client driving the LTI flow, and fall back to all
            // sessions when none carries that client id (e.g. the note was never written because the
            // session was created before performLogin recorded it).
            List<AuthenticationSessionModel> candidates = new ArrayList<>();
            if (rootAuthSession != null) {
                Map<String, AuthenticationSessionModel> sessions = rootAuthSession.getAuthenticationSessions();
                String keycloakClientId = provider.getConfig().getKeycloakClientId();
                for (AuthenticationSessionModel candidate : sessions.values()) {
                    if (candidate.getClient() != null
                        && candidate.getClient().getClientId().equals(keycloakClientId)) {
                        candidates.add(candidate);
                    }
                }
                if (candidates.isEmpty()) {
                    candidates.addAll(sessions.values());
                }
            }

            if (candidates.isEmpty()) {
                log.warnf("LTI launch callback: no matching authentication session found under root " +
                        "session %s (client=%s) - session may have expired, or the browser did not send " +
                        "Keycloak's session cookie back on this cross-site request from the LMS",
                    rootAuthSession != null ? rootAuthSession.getId() : "none",
                    provider.getConfig().getKeycloakClientId());
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No matching authentication session - the LTI login flow may have expired, " +
                        "or the session cookie was not recognized on this callback from the LMS.")
                    .build();
            }

            try {
                BrokeredIdentityContext identity = null;
                AuthenticationSessionModel matchedSession = null;
                Exception lastFailure = null;
                for (AuthenticationSessionModel candidate : candidates) {
                    provider.session.getContext().setAuthenticationSession(candidate);
                    try {
                        identity = provider.validateAndExtract(idToken, state, candidate);
                        matchedSession = candidate;
                        break;
                    } catch (Exception e) {
                        lastFailure = e;
                        // If this candidate merely failed the nonce check, keep looking: a different
                        // (usually newer) session under the same root may carry the nonce for this
                        // particular launch. Any harder error - bad signature, wrong issuer, expired
                        // token - is fatal and aborts the loop.
                        String msg = e.getMessage();
                        boolean nonceOnly = e instanceof IllegalArgumentException
                            && msg != null
                            && (msg.contains("nonce") || msg.contains("No nonce recorded"));
                        if (!nonceOnly) {
                            throw e;
                        }
                    }
                }

                if (identity == null || matchedSession == null) {
                    log.warn("LTI launch validation failed", lastFailure);
                    return callback.error("invalid_lti_launch");
                }

                identity.setIdpConfig(provider.getConfig());
                identity.setIdp(provider);

                // CRITICAL FIX: Ensure authenticationSession is attached to BrokeredIdentityContext
                identity.setAuthenticationSession(matchedSession);

                // Deep linking (LtiDeepLinkingRequest): after a successful broker login, send the teacher to the
                // Cytomine content-selection URL instead of the resource-link target, carrying a short-lived,
                // single-use context token that records where the DeepLinkingResponse must be posted back.
                if (identity.getContextData().get(CTX_DEEP_LINKING_SETTINGS) != null) {
                    return handleDeepLinkingLaunch(identity, matchedSession);
                }

                return callback.authenticated(identity);
            } catch (Exception e) {
                log.warn("LTI launch validation failed", e);
                return callback.error("invalid_lti_launch");
            }
        }

        @SuppressWarnings("unchecked")
        private Response handleDeepLinkingLaunch(BrokeredIdentityContext identity,
                                                 AuthenticationSessionModel authSession) {
            String selectionUrl = provider.getConfig().getContentSelectionUrl();
            if (selectionUrl == null || selectionUrl.isBlank()) {
                log.warnf("LTI deep-linking launch received but this identity provider has no Content " +
                    "Selection URL configured - rejecting the launch");
                return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity("LTI deep linking is not enabled for this identity provider (missing " +
                        "contentSelectionUrl). Contact the administrator.")
                    .build();
            }

            Map<String, Object> dlSettings = (Map<String, Object>) identity.getContextData()
                .get(CTX_DEEP_LINKING_SETTINGS);
            Object returnUrlRaw = dlSettings.get("deep_link_return_url");
            if (!(returnUrlRaw instanceof String returnUrl) || returnUrl.isBlank()) {
                log.warnf("LTI deep-linking launch is missing deep_link_return_url in deep_linking_settings");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("LTI deep linking launch is missing deep_link_return_url")
                    .build();
            }

            Map<?, ?> claims = identity.getContextData().get(CTX_LTI_CLAIMS) instanceof Map<?, ?> c
                ? c
                : Map.of();
            String subject = claims.get("sub") instanceof String s ? s : null;
            String deploymentId = claims.get(CLAIM_DEPLOYMENT_ID) instanceof String s ? s : null;
            Object dataRaw = identity.getContextData().get(CTX_DEEP_LINKING_DATA);

            Map<String, String> returnCtx = new LinkedHashMap<>();
            returnCtx.put(DL_CTX_RETURN_URL, returnUrl);
            if (dataRaw != null) {
                returnCtx.put(DL_CTX_DATA, dataRaw.toString());
            }
            returnCtx.put(DL_CTX_ISS, provider.getConfig().getPlatformIssuer());
            returnCtx.put(DL_CTX_CLIENT_ID, provider.getConfig().getToolClientId());
            if (deploymentId != null) {
                returnCtx.put(DL_CTX_DEPLOYMENT_ID, deploymentId);
            }
            if (subject != null) {
                returnCtx.put(DL_CTX_SUB, subject);
            }

            String ctxToken = UUID.randomUUID().toString();
            try {
                provider.session.singleUseObjects().put(DL_CTX_SINGLE_USE_PREFIX + ctxToken,
                    System.currentTimeMillis() + DL_CTX_TTL_MS, returnCtx);
            } catch (Exception e) {
                log.warnf("LTI deep linking: could not store return context", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("LTI deep linking could not be completed - try again.")
                    .build();
            }

            String pickerUrl = selectionUrl + (selectionUrl.contains("?") ? "&" : "?") + "ctx=" + ctxToken;
            authSession.setRedirectUri(pickerUrl);
            log.infof("LTI deep linking: teacher launch accepted, redirecting to content selection URL %s", pickerUrl);
            return callback.authenticated(identity);
        }
    }

    // Validation and claim extraction
    @SuppressWarnings("unchecked")
    BrokeredIdentityContext validateAndExtract(String rawIdToken, String returnedState,
                                               AuthenticationSessionModel authSession) throws Exception {
        LTIJwtValidator jwtValidator = new LTIJwtValidator(this.session);
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
        boolean deepLinking = "LtiDeepLinkingRequest".equals(messageType);
        if (!"LtiResourceLinkRequest".equals(messageType) && !deepLinking) {
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

        // For deep-linking launches, validate the Content-Item request: the platform must have told us
        // where to post the DeepLinkingResponse (deep_link_return_url) and should include an opaque
        // `data` value to echo back unchanged.
        Map<String, Object> deepLinkingSettings = null;
        String deepLinkingData = null;
        if (deepLinking) {
            deepLinkingSettings = claims.get(CLAIM_DEEP_LINKING_SETTINGS) instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : null;
            if (deepLinkingSettings == null) {
                throw new IllegalArgumentException("LtiDeepLinkingRequest missing deep_linking_settings claim");
            }
            Object returnUrlRaw = deepLinkingSettings.get("deep_link_return_url");
            if (!(returnUrlRaw instanceof String returnUrl) || returnUrl.isBlank()) {
                throw new IllegalArgumentException(
                    "LtiDeepLinkingRequest missing deep_link_return_url in deep_linking_settings");
            }
            if (!returnUrl.startsWith("https://") && !returnUrl.startsWith("http://")) {
                throw new IllegalArgumentException("deep_link_return_url must be an absolute http(s) URL");
            }
            Object dataRaw = deepLinkingSettings.get("data");
            if (dataRaw instanceof String data) {
                deepLinkingData = data;
            }
        }

        if (returnedState == null || returnedState.isBlank()) {
            throw new IllegalArgumentException("Missing state in launch response");
        }

        String pendingNonces = authSession.getClientNote(NOTE_NONCE);
        Object nonceClaim = claims.get("nonce");
        if (pendingNonces == null || pendingNonces.isBlank()) {
            throw new IllegalArgumentException("No nonce recorded for this authentication session - was performLogin() skipped?");
        }
        if (!(nonceClaim instanceof String) || !consumePendingNonce(authSession, (String) nonceClaim)) {
            throw new IllegalArgumentException("nonce mismatch - possible replay of a previous launch");
        }

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

        // --- PRINT CLAIMS TO LOGS FOR DEBUGGING ---
        try {
            String prettyClaims = JsonSerialization.writeValueAsPrettyString(claims);
            log.infof("=== LTI ID TOKEN CLAIMS RECEIVED ===\n%s\n===================================", prettyClaims);
        } catch (Exception e) {
            log.infof("=== LTI ID TOKEN CLAIMS RECEIVED ===\n%s\n===================================", claims.toString());
        }

        String subject = (String) claims.get(getConfig().getSubjectClaim());
        if (subject == null || subject.isBlank()) {
            subject = (String) claims.get("sub");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Missing subject claim");
        }

        // --- EXTRACT USER DETAILS FROM LTI CLAIMS ---
        Map<String, Object> extParams = claims.get(CLAIM_EXT) instanceof Map<?, ?> map
            ? (Map<String, Object>) map
            : Map.of();

        Map<String, Object> customParams = claims.get(CLAIM_CUSTOM) instanceof Map<?, ?> map
            ? (Map<String, Object>) map
            : Map.of();

        Map<String, Object> lisClaim = claims.get(CLAIM_LIS) instanceof Map<?, ?> map
            ? (Map<String, Object>) map
            : Map.of();

        // 1. Email Extraction
        String email = (String) claims.get("email");
        if (email == null || email.isBlank()) {
            email = (String) lisClaim.get("person_sourcedid");
        }
        if (email == null || email.isBlank()) {
            email = (String) customParams.get("user_email");
        }
        if (email == null || email.isBlank()) {
            email = (String) extParams.get("user_email");
        }

        // 2. First and Last Name Extraction
        String firstName = (String) claims.get("given_name");
        String lastName = (String) claims.get("family_name");

        if (firstName == null && extParams.get("user_firstname") != null) {
            firstName = (String) extParams.get("user_firstname");
        }
        if (firstName == null && customParams.get("user_firstname") != null) {
            firstName = (String) customParams.get("user_firstname");
        }

        if (lastName == null && extParams.get("user_lastname") != null) {
            lastName = (String) extParams.get("user_lastname");
        }
        if (lastName == null && customParams.get("user_lastname") != null) {
            lastName = (String) customParams.get("user_lastname");
        }

        // Composite full name fallback
        if ((firstName == null || lastName == null) && claims.get("name") instanceof String fullName && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            if (firstName == null) firstName = parts[0];
            if (lastName == null) lastName = parts.length > 1 ? parts[1] : parts[0];
        }

        // 3. Username Extraction
        // Prioritize Moodle ext parameters, then custom params, then OIDC standard claims
        String username = (String) extParams.get("user_username");
        if (username == null || username.isBlank()) {
            username = (String) customParams.get("user_username");
        }
        if (username == null || username.isBlank()) {
            username = (String) claims.get("preferred_username");
        }
        if (username == null || username.isBlank()) {
            username = (String) claims.get("username");
        }
        if (username == null || username.isBlank()) {
            username = (email != null && !email.isBlank()) ? email : subject;
        }

        // Fallbacks to avoid triggering Keycloak's Update Profile screen
        if (email == null || email.isBlank()) {
            email = username.contains("@") ? username : username + "@lti.local";
        }
        if (firstName == null || firstName.isBlank()) {
            firstName = "LTI";
        }
        if (lastName == null || lastName.isBlank()) {
            lastName = "User (" + subject + ")";
        }

        // Initialize BrokeredIdentityContext and set user profile properties
        BrokeredIdentityContext identity = new BrokeredIdentityContext(subject);
        identity.setIdpConfig(getConfig());
        identity.setAuthenticationSession(authSession);

        identity.setUsername(username);
        identity.setEmail(email);
        identity.setFirstName(firstName);
        identity.setLastName(lastName);

        // Attach claims for identity provider mappers
        identity.getContextData().put(CTX_LTI_CLAIMS, claims);
        identity.getContextData().put(CTX_LTI_ROLES, claims.get(CLAIM_ROLES));
        identity.getContextData().put(CTX_LTI_CONTEXT, claims.get(CLAIM_CONTEXT));
        identity.getContextData().put(CTX_LTI_TARGET_LINK_URI, claims.get(CLAIM_TARGET_LINK_URI));
        identity.getContextData().put(CTX_LTI_CUSTOM, customParams);
        if (deepLinking) {
            identity.getContextData().put(CTX_DEEP_LINKING_SETTINGS, deepLinkingSettings);
            // Only record `data` when the platform actually supplied it: a null value here breaks
            // Keycloak's SerializedBrokeredIdentityContext.serialize() during the first-broker-login flow.
            if (deepLinkingData != null) {
                identity.getContextData().put(CTX_DEEP_LINKING_DATA, deepLinkingData);
            }
        }

        return identity;
    }
}