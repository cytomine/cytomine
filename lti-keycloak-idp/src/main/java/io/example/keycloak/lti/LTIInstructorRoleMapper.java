package io.example.keycloak.lti;

import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import java.util.Collection;

/**
 * Example IdentityProviderMapper: if the LTI launch's roles claim contains
 * the standard LTI 1.3 "Instructor" role URI, grant a Keycloak realm role
 * (default "instructor") to the user on every login. Copy/extend this
 * pattern for Learner, TA, Admin, etc., or to map LTI context (course) into
 * a group instead of a role.
 *
 * Registered automatically for the "lti-1p3" provider type via
 * getCompatibleProviders(). Configure the target role name per-mapper in
 * the admin console the same way built-in "Hardcoded Role" mappers work.
 */
public class LTIInstructorRoleMapper extends AbstractIdentityProviderMapper {

    public static final String PROVIDER_ID = "lti-instructor-role-mapper";
    private static final String LTI_INSTRUCTOR_ROLE =
            "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor";

    @Override
    public String[] getCompatibleProviders() {
        return new String[]{LTIIdentityProviderFactory.PROVIDER_ID};
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
        return "LTI Instructor Role";
    }

    @Override
    public String getHelpText() {
        return "Grants a realm role when the LTI launch's roles claim includes the LTI Instructor role.";
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

    @SuppressWarnings("unchecked")
    private void applyRole(RealmModel realm, UserModel user, BrokeredIdentityContext context) {
        Object rolesClaim = context.getContextData().get("LTI_ROLES");
        if (!(rolesClaim instanceof Collection<?> roles) || !roles.contains(LTI_INSTRUCTOR_ROLE)) {
            return;
        }
        RoleModel role = realm.getRole("instructor");
        if (role != null) {
            user.grantRole(role);
        }
    }
}
