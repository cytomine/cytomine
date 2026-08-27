package be.cytomine.service.security;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.config.MockedUser;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ForbiddenException;
import be.cytomine.mapper.UserMapper;
import be.cytomine.service.PermissionService;

import static be.cytomine.BasicInstanceBuilder.ACL_USER_NO_ACL;
import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
@MockedUser
public class SecurityAclServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    SecurityACLService securityACLService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    UserMapper userMapper;

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void checkIsUserAllowed() {
        Project project = builder.givenAProject();
        UserResponse user = builder.givenAclUserNoAcl();

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.check(project.getId(), project.getClass().getName(), READ)
        );
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.check(project.getId(), project.getClass(), READ)
        );
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.check(project, READ, user)
        );
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.check(project, READ)
        );

        builder.addUserToProject(project, user.username());

        securityACLService.check(project.getId(), project.getClass().getName(), READ);
        securityACLService.check(project.getId(), project.getClass(), READ);
        securityACLService.check(project, READ, user);
        securityACLService.check(project, READ);
    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void checkIfUserIsContainerAdmin() {
        Project project = builder.givenAProject();
        UserResponse user = builder.givenAclUserNoAcl();

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsAdminContainer(project)
        );
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsAdminContainer(project, user)
        );

        builder.addUserToProject(project, user.username(), ADMINISTRATION);

        securityACLService.checkIsAdminContainer(project);
        securityACLService.checkIsAdminContainer(project, user);
    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void hasUserPermission() {
        Project project = builder.givenAProject();
        UserResponse user = builder.givenAclUserNoAcl();

        assertThat(securityACLService.hasPermission(project, READ, false)).isFalse();
        assertThat(securityACLService.hasPermission(project, READ)).isFalse();
        assertThat(securityACLService.hasPermission(project, READ, true)).isTrue();

        builder.addUserToProject(project, user.username());

        assertThat(securityACLService.hasPermission(project, READ, false)).isTrue();
        assertThat(securityACLService.hasPermission(project, READ)).isTrue();
        assertThat(securityACLService.hasPermission(project, READ, true)).isTrue();
    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void hasRightToReadAbstractImage() {
        Project project = builder.givenAProject();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        UserResponse user = builder.givenAclUserNoAcl();

        assertThat(securityACLService.hasRightToReadAbstractImageWithProject(imageInstance.getBaseImage())).isFalse();

        builder.addUserToProject(project, user.username());

        assertThat(securityACLService.hasRightToReadAbstractImageWithProject(imageInstance.getBaseImage())).isTrue();

    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void listAuthorizedProjects() {
        Project project = builder.givenAProject();
        UserResponse user = builder.givenAclUserNoAcl();

        assertThat(securityACLService.getProjectList(user, project.getOntology().getId())).doesNotContain(project);

        permissionService.addPermission(project, user.username(), READ);

        assertThat(securityACLService.getProjectList(user, project.getOntology().getId())).contains(project);

    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void listUserFromProjects() {
        Project project = builder.givenAProject();
        UserResponse user = builder.givenAclUserNoAcl();

        assertThat(securityACLService.getProjectUsers(project)).doesNotContain(user.username());

        permissionService.addPermission(project, user.username(), READ);

        assertThat(securityACLService.getProjectUsers(project)).contains(user.username());

    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void checkSameUser() {
        UserResponse user = builder.givenAclUserNoAcl();
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsSameUser(builder.getUserEntity(builder.givenSuperAdmin()), user)
        );
        securityACLService.checkIsSameUser(user.id(), user);
        securityACLService.checkIsSameUser(builder.getUserEntity(user), builder.givenSuperAdmin());
    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void checkIsAdmin() {
        UserResponse user = builder.givenAclUserNoAcl();
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkAdmin(user)
        );
        securityACLService.checkAdmin(builder.givenSuperAdmin());
    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void checkIsUser() {
        UserResponse user = builder.givenAclUserNoAcl();
        UserResponse guest = builder.givenGuestAcl();

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkAdmin(guest)
        );
        securityACLService.checkUser(user);
        securityACLService.checkUser(builder.givenSuperAdmin());
    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void checkIsGuest() {
        UserResponse user = builder.givenAclUserNoAcl();
        UserResponse guest = builder.givenGuestAcl();

        securityACLService.checkGuest(guest);
        securityACLService.checkGuest(user);
        securityACLService.checkGuest(builder.givenSuperAdmin());
    }

    @WithMockUser(username = ACL_USER_NO_ACL)
    @Test
    void checkNotReadonly() {
        Project project = builder.givenAProject();
        UserResponse user = builder.givenAclUserNoAcl();
        permissionService.addPermission(project, user.username(), READ);

        securityACLService.checkIsNotReadOnly(project);

        project.setMode(EditingMode.READ_ONLY);

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsNotReadOnly(project)
        );

        permissionService.addPermission(project, user.username(), ADMINISTRATION);

        securityACLService.checkIsNotReadOnly(project);
    }

    @WithMockUser(username = SUPERADMIN)
    @Test
    void checkIsUserInProject() {
        Project project = builder.givenAProject();
        UserResponse user = builder.givenUserAclRead();
        assertThat(securityACLService.isUserInProject(user.id(), project)).isFalse();
        builder.addUserToProject(project, user.username());
        assertThat(securityACLService.isUserInProject(user.id(), project)).isTrue();
    }
}
