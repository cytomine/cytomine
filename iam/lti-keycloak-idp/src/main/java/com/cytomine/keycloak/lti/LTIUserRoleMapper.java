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
import java.util.Collections;
import java.util.List;

/**
 * Maps every LTI 1.3 launch onto one of the "core" client's application
 * roles: ADMIN, USER, or GUEST.
 *
 * Both LTI Instructor and Learner/Student/TeachingAssistant roles map to
 * USER - this mapper governs the level of access the user gets in *our*
 * application, not the role they hold in the LMS. If instructor-vs-student
 * distinctions are ever needed downstream, keep reading LTI_ROLES directly
 * from the context data (still available - see LTIIdentityProvider) rather
 * than overloading this ADMIN/USER/GUEST mapping to carry that meaning.
 *
 * ADMIN is intentionally never granted automatically from an LTI launch -
 * grant it manually in the admin console for specific users who need it.
 * GUEST is the fallback when the launch's roles claim carries none of the
 * recognized LTI context roles (e.g. an unusual/custom role, or none at all).
 *
 * On every login (new user AND existing user re-login), this also removes
 * any other core-client role previously granted by this mapper, so a user
 * whose LTI role changes between logins doesn't accumulate stale grants.
 */
public class LTIInstructorRoleMapper extends AbstractIdentityProviderMapper {

    private static final Logger log = Logger.getLogger(LTIInstructorRoleMapper.class);

    public static final String PROVIDER_ID = "lti-core-role-mapper";

    /** clientId of the Keycloak client that owns the ADMIN / USER / GUEST application roles. */
    private static final String CORE_CLIENT_ID = "core";

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_GUEST = "GUEST";
    private static final List<String> MANAGED_ROLES = List.of(ROLE_ADMIN, ROLE_USER, ROLE_GUEST);

    // Standard LTI 1.3 context role URIs that should be treated as USER access.
    private static final String LTI_ROLE_INSTRUCTOR =
        "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor";
    private static final String LTI_ROLE_LEARNER =
        "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner";
    private static final String LTI_ROLE_STUDENT =
        "http://purl.imsglobal.org/vocab/lis/v2/membership#Student";
    private static final String LTI_ROLE_TEACHING_ASSISTANT =
        "http://purl.imsglobal.org/vocab/lis/v2/membership#TeachingAssistant";

    private static final List<String> USER_ROLE_URIS = List.of(
        LTI_ROLE_INSTRUCTOR, LTI_ROLE_LEARNER, LTI_ROLE_STUDENT, LTI_ROLE_TEACHING_ASSISTANT
    );

    @Override
    public String[] getCompatibleProviders() {
        return new String[]{LTIIdentityProviderFactory.PROVIDER_ID};
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
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
        return "LTI Core Role (ADMIN / USER / GUEST)";
    }

    @Override
    public String getHelpText() {
        return "Grants the core client's USER role to any LTI launch carrying a recognized " +
            "Instructor, Learner, Student, or TeachingAssistant role; falls back to GUEST " +
            "otherwise. ADMIN is never granted automatically.";
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user,
                              IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        applyRole(realm, user, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
                                   IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        applyRole(realm, user, context);
    }

    private void applyRole(RealmModel realm, UserModel user, BrokeredIdentityContext context) {
        ClientModel coreClient = realm.getClientByClientId(CORE_CLIENT_ID);
        if (coreClient == null) {
            log.warnf("LTI role mapper: client '%s' not found in realm '%s' - cannot assign core role",
                CORE_CLIENT_ID, realm.getName());
            return;
        }

        String targetRoleName = resolveTargetRole(context);
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

    /**
     * Instructor, Learner, Student, and TeachingAssistant all resolve to USER -
     * this mapper only distinguishes "has a recognized LTI role" (USER) from
     * "does not" (GUEST). ADMIN is out of scope for automatic LTI mapping.
     */
    private String resolveTargetRole(BrokeredIdentityContext context) {
        Object rolesClaim = context.getContextData().get("LTI_ROLES");
        if (rolesClaim instanceof Collection<?> roles) {
            boolean recognized = roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .anyMatch(USER_ROLE_URIS::contains);
            if (recognized) {
                return ROLE_USER;
            }
        }
        return ROLE_GUEST;
    }
}