package com.cytomine.keycloak.lti;
import org.keycloak.models.IdentityProviderModel;


public class LTIIdentityProviderConfig extends IdentityProviderModel {

    public LTIIdentityProviderConfig() {
        super();
    }

    public LTIIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public String getPlatformIssuer() {
        return getConfig().get("platformIssuer");
    }

    public void setPlatformIssuer(String issuer) {
        getConfig().put("platformIssuer", issuer);
    }

    public String getToolClientId() {
        return getConfig().get("toolClientId");
    }

    public void setToolClientId(String clientId) {
        getConfig().put("toolClientId", clientId);
    }

    public String getAllowedDeploymentIds() {
        return getConfig().get("allowedDeploymentIds");
    }

    public void setAllowedDeploymentIds(String ids) {
        getConfig().put("allowedDeploymentIds", ids);
    }

    public String getKeycloakClientId() {
        return getConfig().get("keycloakClientId");
    }

    public void setKeycloakClientId(String clientId) {
        getConfig().put("keycloakClientId", clientId);
    }

    public String getPlatformAuthorizationEndpoint() {
        return getConfig().get("platformAuthorizationEndpoint");
    }

    public void setPlatformAuthorizationEndpoint(String url) {
        getConfig().put("platformAuthorizationEndpoint", url);
    }

    public String getPlatformJwksUrl() {
        return getConfig().get("platformJwksUrl");
    }

    public void setPlatformJwksUrl(String url) {
        getConfig().put("platformJwksUrl", url);
    }

    public String getSubjectClaim() {
        String v = getConfig().get("subjectClaim");
        return v == null || v.isBlank() ? "sub" : v;
    }

    public void setSubjectClaim(String claim) {
        getConfig().put("subjectClaim", claim);
    }

    /**
     * The Cytomine Web-UI "content selection" (picker) URL used for LTI 1.3 deep
     * linking (LtiDeepLinkingRequest launches). Must also be an allowed redirect
     * URI on the Keycloak client representing the tool, since teachers are sent
     * there with a completed login. Null means deep-linking launches are rejected.
     */
    public String getContentSelectionUrl() {
        String v = getConfig().get("contentSelectionUrl");
        return v == null || v.isBlank() ? null : v;
    }

    public void setContentSelectionUrl(String url) {
        getConfig().put("contentSelectionUrl", url);
    }
}
