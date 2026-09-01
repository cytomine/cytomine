package com.cytomine.keycloak.lti;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resource provider handling LTI 1.1 launches at /realms/{realm}/lti11/launch.
 */
public class LTI11LaunchResourceProvider implements RealmResourceProvider {

    private static final Logger log = Logger.getLogger(LTI11LaunchResourceProvider.class);

    // TTL for the one-time launch ticket
    static final int TICKET_TTL_SECONDS = 120;

    private final KeycloakSession session;

    public LTI11LaunchResourceProvider(KeycloakSession session) {
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

    @POST
    @Path("launch")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response launch(MultivaluedMap<String, String> form, @Context UriInfo uriInfo) {
        String consumerKey = firstOrNull(form, "oauth_consumer_key");
        String messageType = firstOrNull(form, "lti_message_type");
        String ltiVersion = firstOrNull(form, "lti_version");

        if (consumerKey == null || !"basic-lti-launch-request".equals(messageType)
                || !"LTI-1p0".equals(ltiVersion)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Not a valid LTI 1.1 basic launch request")
                    .build();
        }

        RealmModel realm = session.getContext().getRealm();

        Optional<IdentityProviderModel> match = realm.getIdentityProvidersStream()
                .filter(idp -> LTI11IdentityProviderFactory.PROVIDER_ID.equals(idp.getProviderId()))
                .filter(idp -> consumerKey.equals(idp.getConfig().get("consumerKey")))
                .findFirst();

        if (match.isEmpty()) {
            log.warnf("LTI 1.1 launch: no configured IdP for consumer_key=%s", consumerKey);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No LTI 1.1 identity provider is configured for this consumer key")
                    .build();
        }

        IdentityProviderModel idp = match.get();
        String sharedSecret = idp.getConfig().get("sharedSecret");
        String keycloakClientId = idp.getConfig().get("keycloakClientId");
        String toolRedirectUri = idp.getConfig().get("toolRedirectUri");

        if (sharedSecret == null || keycloakClientId == null || toolRedirectUri == null) {
            return Response.serverError()
                    .entity("Matched LTI 1.1 identity provider is missing required configuration")
                    .build();
        }

        // Exact URL the LMS signed against
        String requestUrl = uriInfo.getRequestUri().toString();
        // Strip any query string
        int q = requestUrl.indexOf('?');
        if (q >= 0) requestUrl = requestUrl.substring(0, q);

        Map<String, List<String>> params = form.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        boolean valid;
        try {
            valid = OAuth1LaunchValidator.verify("POST", requestUrl, params, sharedSecret);
        } catch (Exception e) {
            log.warn("OAuth1 verification error on LTI 1.1 launch", e);
            valid = false;
        }

        if (!valid) {
            log.warnf("LTI 1.1 launch signature verification FAILED for consumer_key=%s", consumerKey);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Invalid OAuth signature")
                    .build();
        }

        String subjectParam = idp.getConfig().getOrDefault("subjectParam", "user_id");
        String userId = firstOrNull(form, subjectParam);
        if (userId == null || userId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Launch is missing " + subjectParam)
                    .build();
        }

        // Namespace subject with consumer key for per-consumer uniqueness
        String namespacedSubject = consumerKey + ":" + userId;

        String ticket = UUID.randomUUID().toString();
        session.singleUseObjects().put(ticketKey(ticket), TICKET_TTL_SECONDS, toTicketData(form, namespacedSubject));

        UriBuilder authUrl = UriBuilder.fromUri(session.getContext().getUri().getBaseUri())
                .path("realms").path(realm.getName())
                .path("protocol/openid-connect/auth")
                .queryParam("client_id", keycloakClientId)
                .queryParam("redirect_uri", toolRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("kc_idp_hint", idp.getAlias())
                .queryParam("login_hint", ticket);

        return Response.seeOther(authUrl.build()).build();
    }

    static String ticketKey(String ticket) {
        return "lti11-launch-ticket:" + ticket;
    }

    private static Map<String, String> toTicketData(MultivaluedMap<String, String> form, String namespacedSubject) {
        Map<String, String> data = new java.util.HashMap<>();
        data.put("sub", namespacedSubject);
        putIfPresent(data, form, "lis_person_contact_email_primary", "email");
        putIfPresent(data, form, "lis_person_name_given", "given_name");
        putIfPresent(data, form, "lis_person_name_family", "family_name");
        putIfPresent(data, form, "roles", "roles");
        putIfPresent(data, form, "context_id", "context_id");
        putIfPresent(data, form, "context_title", "context_title");
        putIfPresent(data, form, "resource_link_id", "resource_link_id");
        return data;
    }

    private static void putIfPresent(Map<String, String> data, MultivaluedMap<String, String> form,
                                       String formKey, String dataKey) {
        String v = firstOrNull(form, formKey);
        if (v != null) data.put(dataKey, v);
    }

    private static String firstOrNull(MultivaluedMap<String, String> form, String key) {
        List<String> v = form.get(key);
        return (v == null || v.isEmpty()) ? null : v.get(0);
    }
}
