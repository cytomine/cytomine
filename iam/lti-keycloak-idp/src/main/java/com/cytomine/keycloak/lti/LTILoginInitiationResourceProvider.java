package com.cytomine.keycloak.lti;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;

import java.util.Optional;

public class LTILoginInitiationResourceProvider implements RealmResourceProvider {

    private static final Logger log = Logger.getLogger(LTILoginInitiationResourceProvider.class);

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

    @POST
    @Path("login-init")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response loginInit(@FormParam("iss") String iss,
                               @FormParam("login_hint") String loginHint,
                               @FormParam("target_link_uri") String targetLinkUri,
                               @FormParam("client_id") String clientId,
                               @FormParam("lti_deployment_id") String deploymentId,
                               @FormParam("lti_message_hint") String messageHint) {

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
                .queryParam("login_hint", packedHint);

        return Response.seeOther(authUrl.build()).build();
    }
}
