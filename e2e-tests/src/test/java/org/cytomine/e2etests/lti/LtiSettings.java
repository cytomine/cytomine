package org.cytomine.e2etests.lti;

/**
 * Central configuration for the LTI wire-level contract suite. Values default to the dev
 * compose deployment (iam on :8100) and can be overridden through environment variables so
 * the same suite can run inside CI pipelines against a different stack location.
 */
final class LtiSettings {

    /** Base URL of the IAM service with its relative path, e.g. http://localhost:8100/iam. */
    static final String IAM_BASE_URL = setting("LTI_E2E_IAM_BASE_URL", "http://localhost:8100/iam");

    /** Dedicated Keycloak realm created (and dropped) by the suite. */
    static final String REALM = "lti-e2e";

    /** Master realm credentials, matching the compose KEYCLOAK_ADMIN + KEYCLOAK_ADMIN_PASSWORD. */
    static final String IAM_ADMIN = setting("LTI_E2E_KEYCLOAK_ADMIN", "admin");
    static final String IAM_ADMIN_PASSWORD = setting("LTI_E2E_KEYCLOAK_ADMIN_PASSWORD", "masterpassword");

    /** Port of the embedded platform simulator. Keycloak reaches it through the compose extra_hosts
     * mapping for "localhost", so the value must stay fixed once the IdPs are seeded with URLs. */
    static final int SIMULATOR_PORT = Integer.parseInt(setting("LTI_E2E_MOCK_PORT", "8102"));

    private LtiSettings() {
    }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    static String realmBase() {
        return IAM_BASE_URL + "/realms/" + REALM;
    }

    static String realmJwksUrl() {
        return realmBase() + "/protocol/openid-connect/certs";
    }

    static String loginInitUrl() {
        return realmBase() + "/lti/login-init";
    }

    static String deepLinkReturnUrl() {
        return realmBase() + "/lti/dl-return";
    }

    static String simulatorBase() {
        return "http://localhost:" + SIMULATOR_PORT;
    }
}
