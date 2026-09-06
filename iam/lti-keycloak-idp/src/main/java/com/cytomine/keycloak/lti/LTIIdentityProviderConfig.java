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
}
