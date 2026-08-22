package com.cytomine.keycloak.lti;
import org.keycloak.models.IdentityProviderModel;

/**
 * One instance == one LTI 1.1 "consumer" registration (an LMS course tool
 * config, or an entire LMS install if it shares one key/secret). LTI 1.1 has
 * no issuer/JWKS concept - trust is entirely the shared oauth_consumer_key /
 * shared secret pair, so keep the secret as secret as any client secret.
 */
public class LTI11IdentityProviderConfig extends IdentityProviderModel {

    public LTI11IdentityProviderConfig() {
        super();
    }

    public LTI11IdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public String getConsumerKey() {
        return getConfig().get("consumerKey");
    }

    public void setConsumerKey(String key) {
        getConfig().put("consumerKey", key);
    }

    public String getSharedSecret() {
        return getConfig().get("sharedSecret");
    }

    public void setSharedSecret(String secret) {
        getConfig().put("sharedSecret", secret);
    }

    /** The Keycloak client (already registered in this realm) representing the protected tool. */
    public String getKeycloakClientId() {
        return getConfig().get("keycloakClientId");
    }

    public void setKeycloakClientId(String clientId) {
        getConfig().put("keycloakClientId", clientId);
    }

    /**
     * LTI 1.1 has no dynamic target_link_uri like 1.3 does - the launch URL
     * *is* usually where the tool lives. If your tool is served from a
     * separate URL than the launch endpoint, set the fixed redirect target
     * here (must match a Valid Redirect URI on the Keycloak client above).
     */
    public String getToolRedirectUri() {
        return getConfig().get("toolRedirectUri");
    }

    public void setToolRedirectUri(String uri) {
        getConfig().put("toolRedirectUri", uri);
    }

    /**
     * Claim/param used as the stable per-user identifier. LTI 1.1's
     * "user_id" plays the role LTI 1.3's "sub" does - stable per consumer,
     * not guaranteed globally unique, so it's namespaced with the consumer
     * key when building the Keycloak federated identity ID (see
     * LTI11IdentityProvider).
     */
    public String getSubjectParam() {
        String v = getConfig().get("subjectParam");
        return v == null || v.isBlank() ? "user_id" : v;
    }

    public void setSubjectParam(String param) {
        getConfig().put("subjectParam", param);
    }
}
