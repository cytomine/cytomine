package com.cytomine.keycloak.lti;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Map;
import jakarta.ws.rs.core.Response;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.FederatedIdentityModel;

/**
 * Unlike LTI 1.3, there's no external platform to redirect to: verification
 * already happened synchronously in {@link LTI11LaunchResourceProvider}
 * before the browser was ever sent into Keycloak's broker flow. So
 * performLogin() here just forwards the pre-verified ticket to this
 * provider's own callback endpoint (one internal redirect, no round trip to
 * the LMS), and callback() resolves the ticket into a BrokeredIdentityContext.
 */
public class LTI11IdentityProvider extends AbstractIdentityProvider<LTI11IdentityProviderConfig> {

    private static final Logger log = Logger.getLogger(LTI11IdentityProvider.class);

    @Override
    public Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).build();
    }

    public LTI11IdentityProvider(KeycloakSession session, LTI11IdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    public Response performLogin(AuthenticationRequest request) {
        AuthenticationSessionModel authSession = request.getAuthenticationSession();
        String ticket = authSession.getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        if (ticket == null) {
            throw new IdentityBrokerException(
                    "Missing LTI 1.1 launch ticket - this IdP must be entered via /lti11/launch, not directly.");
        }

        UriBuilder callback = UriBuilder.fromUri(request.getRedirectUri())
                .queryParam("ticket", ticket);
        return Response.seeOther(callback.build()).build();
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new Endpoint(this, callback);
    }

    public static class Endpoint {
        private final AuthenticationCallback callback;
        private final LTI11IdentityProvider provider;

        public Endpoint(LTI11IdentityProvider provider,AuthenticationCallback callback) {
            this.callback = callback;
            this.provider = provider;
        }

        @GET
        public Response consumeTicket(@QueryParam("ticket") String ticket) {
            if (ticket == null) {
                return callback.error("missing_ticket");
            }

            Map<String, String> data = provider.session.singleUseObjects()
                    .remove(LTI11LaunchResourceProvider.ticketKey(ticket));

            if (data == null) {
                // expired, already consumed, or forged - reject either way
                log.warn("LTI 1.1 callback: unknown or expired launch ticket");
                return callback.error("invalid_or_expired_ticket");
            }

            try {
                BrokeredIdentityContext identity = provider.buildIdentity(data);
                return callback.authenticated(identity);
            } catch (Exception e) {
                log.warn("Failed to build identity from LTI 1.1 launch data", e);
                return callback.error("invalid_lti_launch");
            }
        }
    }

    BrokeredIdentityContext buildIdentity(Map<String, String> data) {
        String subject = data.get("sub");
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Launch ticket missing subject");
        }

        BrokeredIdentityContext identity = new BrokeredIdentityContext(subject);
        identity.setIdpConfig(getConfig());
        identity.setUsername(subject);
        if (data.get("email") != null) identity.setEmail(data.get("email"));
        if (data.get("given_name") != null) identity.setFirstName(data.get("given_name"));
        if (data.get("family_name") != null) identity.setLastName(data.get("family_name"));

        identity.setIdpConfig(getConfig());
        identity.setIdp(this);

        // "roles" here is LTI 1.1's comma-separated role list, e.g.
        // "Instructor,urn:lti:instrole:ims/lis/Instructor" - split on demand
        // in a mapper (see LTIInstructorRoleMapper for the 1.3 equivalent;
        // a 1.1 role mapper would just split on comma instead of reading a
        // JSON array claim).
        if (data.get("roles") != null) {
            identity.getContextData().put("LTI11_ROLES", data.get("roles").split("\\s*,\\s*"));
        }
        identity.getContextData().put("LTI11_CONTEXT_ID", data.get("context_id"));
        identity.getContextData().put("LTI11_CONTEXT_TITLE", data.get("context_title"));
        identity.getContextData().put("LTI11_RESOURCE_LINK_ID", data.get("resource_link_id"));

        return identity;
    }
}
