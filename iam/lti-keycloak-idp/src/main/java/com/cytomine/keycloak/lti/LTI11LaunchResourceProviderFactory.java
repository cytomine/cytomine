package com.cytomine.keycloak.lti;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * Factory for LTI 1.1 realm resource endpoints under /realms/{realm}/lti11.
 */
public class LTI11LaunchResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "lti11";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new LTI11LaunchResourceProvider(session);
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
