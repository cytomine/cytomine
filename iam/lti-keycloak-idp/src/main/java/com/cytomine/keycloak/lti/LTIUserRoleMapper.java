package com.cytomine.keycloak.lti;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LTIUserRoleMapper extends AbstractIdentityProviderMapper {

    private static final Logger log = Logger.getLogger(LTIUserRoleMapper.class);

    public static final String PROVIDER_ID = "lti-core-role-mapper";

    private static final String CORE_CLIENT_ID = "core";

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_GUEST = "GUEST";
    private static final List<String> MANAGED_ROLES = List.of(ROLE_ADMIN, ROLE_USER, ROLE_GUEST);

    // Admin-console config keys: one comma-separated LTI-role list per Keycloak role.
    private static final String CONFIG_ADMIN_ROLE_URIS = "adminRoleUris";
    private static final String CONFIG_USER_ROLE_URIS = "userRoleUris";
    private static final String CONFIG_GUEST_ROLE_URIS = "guestRoleUris";

    private static final Map<String, String> SHORT_NAME_TO_URI = new LinkedHashMap<>();
    static {
        SHORT_NAME_TO_URI.put("Instructor", "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor");
        SHORT_NAME_TO_URI.put("Learner", "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
        SHORT_NAME_TO_URI.put("Student", "http://purl.imsglobal.org/vocab/lis/v2/membership#Student");
        SHORT_NAME_TO_URI.put("TeachingAssistant", "http://purl.imsglobal.org/vocab/lis/v2/membership#TeachingAssistant");
        SHORT_NAME_TO_URI.put("Administrator", "http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator");
        SHORT_NAME_TO_URI.put("ContentDeveloper", "http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper");
        SHORT_NAME_TO_URI.put("Mentor", "http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor");
    }

    private static final String DEFAULT_ADMIN_ROLES_CSV = "";
    private static final String DEFAULT_USER_ROLES_CSV = "Instructor,TeachingAssistant";
    private static final String DEFAULT_GUEST_ROLES_CSV = "Student,Learner";

    @Override
    public String[] getCompatibleProviders() {
        return new String[]{LTIIdentityProviderFactory.PROVIDER_ID};
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty adminRoles = new ProviderConfigProperty();
        adminRoles.setName(CONFIG_ADMIN_ROLE_URIS);
        adminRoles.setLabel("LTI roles mapped to ADMIN");
        adminRoles.setHelpText(
            "Comma-separated LTI roles that should be granted ADMIN. Leave blank (default) to keep " +
                "ADMIN unreachable from LTI launches - grant it manually instead. Only populate this if you " +
                "trust the LMS's role claims for your highest privilege tier. Short names: Instructor, " +
                "Learner, Student, TeachingAssistant, Administrator, ContentDeveloper, Mentor - or paste a " +
                "full IMS role URI for a custom role.");
        adminRoles.setType(ProviderConfigProperty.STRING_TYPE);
        adminRoles.setDefaultValue(DEFAULT_ADMIN_ROLES_CSV);

        ProviderConfigProperty userRoles = new ProviderConfigProperty();
        userRoles.setName(CONFIG_USER_ROLE_URIS);
        userRoles.setLabel("LTI roles mapped to USER");
        userRoles.setHelpText(
            "Comma-separated LTI roles that should be granted USER. Same short names / custom URI " +
                "rules as above. Default: " + DEFAULT_USER_ROLES_CSV + ".");
        userRoles.setType(ProviderConfigProperty.STRING_TYPE);
        userRoles.setDefaultValue(DEFAULT_USER_ROLES_CSV);

        ProviderConfigProperty guestRoles = new ProviderConfigProperty();
        guestRoles.setName(CONFIG_GUEST_ROLE_URIS);
        guestRoles.setLabel("LTI roles mapped to GUEST (optional)");
        guestRoles.setHelpText(
            "Comma-separated LTI roles that should be granted GUEST explicitly. Purely documentary in " +
                "most setups: any launch role not matched above already falls back to GUEST, whether or not " +
                "it's listed here. Leave blank unless you want the mapping spelled out explicitly.");
        guestRoles.setType(ProviderConfigProperty.STRING_TYPE);
        guestRoles.setDefaultValue(DEFAULT_GUEST_ROLES_CSV);

        return List.of(adminRoles, userRoles, guestRoles);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayCategory() {
        return "Role Importer";
    }

    @Override
    public String getDisplayType() {
        return "LTI Core Roles";
    }

    @Override
    public String getHelpText() {
        return "Grants the core client's ADMIN / USER / GUEST role based on three admin-configurable " +
            "comma-separated LTI-role lists (one per Keycloak role). Precedence is ADMIN > USER > GUEST; " +
            "unmatched launches fall back to GUEST. ADMIN's list is empty by default, so ADMIN is not " +
            "granted automatically unless explicitly configured.";
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user,
                              IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        applyRole(realm, user, mapperModel, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
                                   IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        applyRole(realm, user, mapperModel, context);
    }

    private void applyRole(RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel,
                           BrokeredIdentityContext context) {
        ClientModel coreClient = realm.getClientByClientId(CORE_CLIENT_ID);
        if (coreClient == null) {
            log.warnf("LTI role mapper: client '%s' not found in realm '%s' - cannot assign core role",
                CORE_CLIENT_ID, realm.getName());
            return;
        }

        String targetRoleName = resolveTargetRole(mapperModel, context);
        RoleModel targetRole = coreClient.getRole(targetRoleName);
        if (targetRole == null) {
            log.warnf("LTI role mapper: role '%s' not found on client '%s' - has it been created yet?",
                targetRoleName, CORE_CLIENT_ID);
            return;
        }

        for (String candidate : MANAGED_ROLES) {
            if (candidate.equals(targetRoleName)) continue;
            RoleModel stale = coreClient.getRole(candidate);
            if (stale != null && user.hasRole(stale)) {
                user.deleteRoleMapping(stale);
            }
        }

        if (!user.hasRole(targetRole)) {
            user.grantRole(targetRole);
        }
    }


    private String resolveTargetRole(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        Set<String> launchRoles = extractLaunchRoles(context);

        if (matchesAny(launchRoles, resolveConfiguredUris(mapperModel, CONFIG_ADMIN_ROLE_URIS, DEFAULT_ADMIN_ROLES_CSV))) {
            return ROLE_ADMIN;
        }
        if (matchesAny(launchRoles, resolveConfiguredUris(mapperModel, CONFIG_USER_ROLE_URIS, DEFAULT_USER_ROLES_CSV))) {
            return ROLE_USER;
        }

        matchesAny(launchRoles, resolveConfiguredUris(mapperModel, CONFIG_GUEST_ROLE_URIS, DEFAULT_GUEST_ROLES_CSV));

        return ROLE_GUEST;
    }

    private Set<String> extractLaunchRoles(BrokeredIdentityContext context) {
        Object rolesClaim = context.getContextData().get("LTI_ROLES");
        Set<String> roles = new LinkedHashSet<>();
        if (rolesClaim instanceof Collection<?> collection) {
            collection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .forEach(roles::add);
        }
        return roles;
    }

    private boolean matchesAny(Set<String> launchRoles, Set<String> configuredUris) {
        return launchRoles.stream().anyMatch(configuredUris::contains);
    }

    private Set<String> resolveConfiguredUris(IdentityProviderMapperModel mapperModel, String configKey, String defaultCsv) {
        String csv = mapperModel.getConfig() == null ? null : mapperModel.getConfig().get(configKey);
        if (csv == null || csv.isBlank()) {
            csv = defaultCsv;
        }

        Set<String> uris = new LinkedHashSet<>();
        for (String token : csv.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            uris.add(SHORT_NAME_TO_URI.getOrDefault(trimmed, trimmed));
        }
        return uris;
    }
}