package be.cytomine.authorization.security;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.AbstractAuthorizationTest;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.service.project.ProjectMemberService;
import be.cytomine.service.search.UserSearchExtension;
import be.cytomine.service.security.SecUserSecRoleService;
import be.cytomine.service.security.UserService;
import be.cytomine.utils.JsonObject;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class UserAuthorizationTest extends AbstractAuthorizationTest {

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    UserService userService;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    SecUserSecRoleService secSecUserSecRoleService;

    @Test
    @WithMockUser(username = GUEST)
    public void everyBodyCanReadUser() {
        User userNoAcl = userRepository.findByUsernameLikeIgnoreCase(USER_NO_ACL).get();
        assertThat(userService.findUser(userNoAcl.getId())).isPresent();
        assertThat(userService.find(userNoAcl.getId())).isPresent();
        assertThat(userService.get(userNoAcl.getId())).isNotNull();
        assertThat(userService.findByUsername(userNoAcl.getUsername())).isPresent();
        assertThat(userService.findByPublicKey(((User) userNoAcl).getPublicKey())).isPresent();
        assertThat(userService.getAuthenticationRoles(userNoAcl)).isNotNull();
    }

    @Test
    @WithMockUser(username = GUEST)
    public void everyBodyListUser() {
        userService.list(new ArrayList<>(), "created", "desc", 0L, 0L);
    }

    @Test
    @WithMockUser(username = GUEST)
    public void everyBodyCannotListUserFromProject() {
        expectForbidden(() -> userService.listUsersExtendedByProject(
            builder.givenAProject(),
            new UserSearchExtension(),
            new ArrayList<>(),
            "created",
            "desc",
            0L,
            0L
        ));
        expectForbidden(() -> userService.listUsersByProject(
            builder.givenAProject(),
            new ArrayList<>(),
            "created",
            "desc",
            0L,
            0L
        ));
        expectForbidden(() -> userService.listAdmins(builder.givenAProject()));
        expectForbidden(() -> userService.listUsers(builder.givenAProject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userProjectCanListUserFromProject() {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, USER_ACL_READ);
        expectOK(() -> userService.listUsersExtendedByProject(
            project,
            new UserSearchExtension(),
            new ArrayList<>(),
            "created",
            "desc",
            0L,
            0L
        ));
        expectOK(() -> userService.listAdmins(project));
        expectOK(() -> userService.listUsers(project));
    }

    // TODO IAM
    @Disabled
    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCanAddUser() {
        User user = builder.givenANotPersistedUser();
        expectOK(() -> userService.add(user.toJsonObject().withChange("password", UUID.randomUUID().toString())));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCanModifyHimself() {
        User user = userRepository.findByUsernameLikeIgnoreCase(USER_NO_ACL).get();
        JsonObject userJson = user.toJsonObject()
            .withChange("name", "user_can_modify_himself")
            .withChange("reference", UUID.randomUUID().toString());
        expectOK(() -> userService.update(user, userJson));
        assertThat(user.getName()).isEqualTo("user_can_modify_himself");
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void shouldUpdateUserNameWhenAdminModifiesUser() {
        User user = userRepository.findByUsernameLikeIgnoreCase(USER_NO_ACL).get();
        JsonObject userJson = user.toJsonObject()
            .withChange("name", "admin_can_modify_a_user")
            .withChange("reference", UUID.randomUUID().toString());
        expectOK(() -> userService.update(user, userJson));
        assertThat(user.getName()).isEqualTo("admin_can_modify_a_user");
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCannotModifyAnotherUser() {
        User user = userRepository.findByUsernameLikeIgnoreCase(GUEST).get();
        expectForbidden(() -> userService.update(
            user,
            user.toJsonObject().withChange("name", "user_can_modify_himself")
        ));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCannotDeleteAnotherUser() {
        User user = builder.givenAUser();
        expectForbidden(() -> userService.delete(user, null, null, false));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCannotDeleteHimself() {
        User user = userRepository.findByUsernameLikeIgnoreCase(USER_NO_ACL).get();
        expectForbidden(() -> userService.delete(user, null, null, false));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanDeleteAnotherUser() {
        User user = builder.givenAUser();
        expectOK(() -> userService.delete(user, null, null, false));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void shouldAddAndRemoveUserFromProjectWhenAdmin() {
        User user = builder.givenAUser();
        Project project = builder.givenAProject();
        expectOK(() -> projectMemberService.addUserToProject(user, project, false));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void shouldAddAndRemoveUserFromProjectWhenUserHasAdminRight() {
        User user = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, USER_ACL_ADMIN, ADMINISTRATION);
        expectOK(() -> projectMemberService.addUserToProject(user, project, false));
        expectOK(() -> projectMemberService.deleteUserFromProject(user, project, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void shouldDenyAddAndRemoveUserFromProjectWhenUserHasReadRight() {
        User user = builder.givenAUser();
        Project project = builder.givenAProject();
        builder.addUserToProject(project, USER_ACL_READ, READ);
        expectForbidden(() -> projectMemberService.addUserToProject(user, project, false));
        expectForbidden(() -> projectMemberService.deleteUserFromProject(user, project, false));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void shouldDenyGrantingAdminRoleWhenUserIsNotSuperAdmin() {
        User user = builder.givenAUser();
        expectForbidden(() -> secSecUserSecRoleService.add(builder.givenANotPersistedUserRole(user, "ROLE_ADMIN")
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void shouldGrantAdminRoleWhenSuperAdmin() {
        User user = builder.givenAUser();
        expectOK(() -> secSecUserSecRoleService.add(builder.givenANotPersistedUserRole(user, "ROLE_ADMIN")
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void shouldDenyRevokingRoleWhenUserIsNotSuperAdmin() {
        User user = builder.givenAUser();
        expectForbidden(() -> secSecUserSecRoleService.delete(builder.givenAUserRole(user), null, null, false));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void shouldRevokeRoleWhenSuperAdmin() {
        User user = builder.givenAUser();
        expectOK(() -> secSecUserSecRoleService.delete(builder.givenAUserRole(user), null, null, false));
    }
}
