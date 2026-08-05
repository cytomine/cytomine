package com.cytomine.keycloak.lti;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * Registers the "lti" realm resource, i.e. everything under
 * /realms/{realm}/lti/... (currently just POST /lti/login-init).
 *
 * Discovered via
 * META-INF/services/org.keycloak.services.resource.RealmResourceProviderFactory
 */
public class LTILoginInitiationResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "lti";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new LTILoginInitiationResourceProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return ID;
    }
}
