package com.cytomine.keycloak.lti;
import org.keycloak.models.IdentityProviderModel;

/**
 * Typed accessors over the generic key/value config that Keycloak stores for
 * every Identity Provider instance. Each field here becomes one input field
 * on the "Add identity provider" admin console page (see
 * {@link LTIIdentityProviderFactory#getConfigProperties()}).
 *
 * One instance of this config == one registered LTI Platform (e.g. one
 * Moodle install, or one Canvas tenant). If you need to support several
 * platforms, add several Identity Provider instances (aliases), each with
 * its own config - same pattern as adding multiple SAML IdPs.
 */
public class LTIIdentityProviderConfig extends IdentityProviderModel {

    public LTIIdentityProviderConfig() {
        super();
    }

    public LTIIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** The LMS's issuer ("iss" claim in the launch id_token). */
    public String getPlatformIssuer() {
        return getConfig().get("platformIssuer");
    }

    public void setPlatformIssuer(String issuer) {
        getConfig().put("platformIssuer", issuer);
    }

    /** The client_id the LMS assigned to *your tool* when you registered it. */
    public String getToolClientId() {
        return getConfig().get("toolClientId");
    }

    public void setToolClientId(String clientId) {
        getConfig().put("toolClientId", clientId);
    }

    /**
     * Comma-separated list of deployment_ids you accept from this platform.
     * A platform may issue several deployment_ids (e.g. one per sub-account);
     * reject anything not in this list.
     */
    public String getAllowedDeploymentIds() {
        return getConfig().get("allowedDeploymentIds");
    }

    public void setAllowedDeploymentIds(String ids) {
        getConfig().put("allowedDeploymentIds", ids);
    }

    /**
     * The Keycloak client (client_id, already registered in this realm)
     * that represents your protected tool. The /lti/login-init endpoint
     * targets this client when kicking off the broker login.
     */
    public String getKeycloakClientId() {
        return getConfig().get("keycloakClientId");
    }

    public void setKeycloakClientId(String clientId) {
        getConfig().put("keycloakClientId", clientId);
    }

    /** LMS's OIDC third-party-initiated-login authorization endpoint. */
    public String getPlatformAuthorizationEndpoint() {
        return getConfig().get("platformAuthorizationEndpoint");
    }

    public void setPlatformAuthorizationEndpoint(String url) {
        getConfig().put("platformAuthorizationEndpoint", url);
    }

    /** LMS's JWKS URL, used to verify the signature on the launch id_token. */
    public String getPlatformJwksUrl() {
        return getConfig().get("platformJwksUrl");
    }

    public void setPlatformJwksUrl(String url) {
        getConfig().put("platformJwksUrl", url);
    }

    /**
     * Claim path used as the unique, stable user identifier. LTI guarantees
     * "sub" is stable per platform+deployment, so this is the sane default -
     * exposed as a config field in case you need to override it.
     */
    public String getSubjectClaim() {
        String v = getConfig().get("subjectClaim");
        return v == null || v.isBlank() ? "sub" : v;
    }

    public void setSubjectClaim(String claim) {
        getConfig().put("subjectClaim", claim);
    }
}
