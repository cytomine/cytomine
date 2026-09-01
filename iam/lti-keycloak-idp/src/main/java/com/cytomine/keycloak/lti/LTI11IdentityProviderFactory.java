package com.cytomine.keycloak.lti;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Identity provider factory for LTI 1.1 legacy LMS launches.
 */
public class LTI11IdentityProviderFactory
        extends AbstractIdentityProviderFactory<LTI11IdentityProvider> {

    public static final String PROVIDER_ID = "lti-1p1";

    @Override
    public String getName() {
        return "LTI 1.1 (Legacy LMS Launch)";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public LTI11IdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new LTI11IdentityProvider(session, new LTI11IdentityProviderConfig(model));
    }

    @Override
    public LTI11IdentityProviderConfig createConfig() {
        return new LTI11IdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> props = new ArrayList<>();

        props.add(property(
                "consumerKey", "OAuth Consumer Key",
                "The oauth_consumer_key you hand the LMS when registering this tool (basic LTI launch), and that it sends back on every launch.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "sharedSecret", "Shared Secret",
                "The OAuth 1.0a shared secret paired with the consumer key. Keep this as protected as a client secret.",
                ProviderConfigProperty.PASSWORD));

        props.add(property(
                "keycloakClientId", "Keycloak Client ID (your tool)",
                "The client_id, already registered in this realm, that represents the protected tool.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "toolRedirectUri", "Tool Redirect URI",
                "Fixed URL to send the browser to after a successful launch (LTI 1.1 has no dynamic target_link_uri). Must match a Valid Redirect URI on the Keycloak client above.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "subjectParam", "Subject Parameter (advanced)",
                "LTI 1.1 launch parameter used as the stable per-user identifier. Defaults to \"user_id\".",
                ProviderConfigProperty.STRING_TYPE));

        return props;
    }

    private static ProviderConfigProperty property(String name, String label, String helpText, String type) {
        ProviderConfigProperty p = new ProviderConfigProperty();
        p.setName(name);
        p.setLabel(label);
        p.setHelpText(helpText);
        p.setType(type);
        return p;
    }
}
