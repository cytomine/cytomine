package io.example.keycloak.lti;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers "LTI 1.3" as a selectable Identity Provider type in
 * Admin Console -> Identity Providers -> Add provider.
 *
 * Discovered via META-INF/services/org.keycloak.broker.provider.IdentityProviderFactory
 */
public class LTIIdentityProviderFactory
        extends AbstractIdentityProviderFactory<LTIIdentityProvider> {

    // Shown in the admin console dropdown, and used as the provider "type".
    public static final String PROVIDER_ID = "lti-1p3";

    @Override
    public String getName() {
        return "LTI 1.3 (LMS Launch)";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public LTIIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new LTIIdentityProvider(session, new LTIIdentityProviderConfig(model));
    }

    @Override
    public LTIIdentityProviderConfig createConfig() {
        return new LTIIdentityProviderConfig();
    }

    /**
     * These become the input fields on the "Add LTI 1.3 provider" form.
     * Values entered here are what LTIIdentityProviderConfig reads back.
     */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> props = new ArrayList<>();

        props.add(property(
                "platformIssuer", "Platform Issuer",
                "The LMS's issuer URL (the \"iss\" value it sends on launch, " +
                        "and publishes in its LTI platform configuration).",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "toolClientId", "Tool Client ID",
                "The client_id the LMS assigned to this tool when you registered it as an LTI tool.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "allowedDeploymentIds", "Allowed Deployment IDs",
                "Comma-separated deployment_id values you accept from this platform.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "keycloakClientId", "Keycloak Client ID (your tool)",
                "The client_id, already registered in this realm, that represents the protected tool. " +
                        "The LTI login-init endpoint starts the browser login against this client.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "platformAuthorizationEndpoint", "Platform Authorization Endpoint",
                "The LMS's OIDC third-party-initiated-login authorization endpoint URL.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "platformJwksUrl", "Platform JWKS URL",
                "The LMS's public JWKS endpoint, used to verify the signature on launch id_tokens.",
                ProviderConfigProperty.STRING_TYPE));

        props.add(property(
                "subjectClaim", "Subject Claim (advanced)",
                "Claim used as the stable per-user identifier. Defaults to \"sub\" - leave blank unless you have a reason to override it.",
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
