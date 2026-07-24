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
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.ForbiddenException;
import be.cytomine.mapper.UserMapper;
import be.cytomine.service.PermissionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class SecurityAclServiceTests {

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    SecurityACLService securityACLService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    UserMapper userMapper;

    @WithMockUser(username = "user")
    @Test
    void checkIsUserAllowed() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

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
            () -> securityACLService.check(project, READ, userMapper.map(user))
        );
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.check(project, READ)
        );

        builder.addUserToProject(project, user.getUsername());

        securityACLService.check(project.getId(), project.getClass().getName(), READ);
        securityACLService.check(project.getId(), project.getClass(), READ);
        securityACLService.check(project, READ, userMapper.map(user));
        securityACLService.check(project, READ);
    }

    @WithMockUser(username = "user")
    @Test
    void checkIfUserIsContainerAdmin() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsAdminContainer(project)
        );
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsAdminContainer(project, userMapper.map(user))
        );

        builder.addUserToProject(project, user.getUsername(), ADMINISTRATION);

        securityACLService.checkIsAdminContainer(project);
        securityACLService.checkIsAdminContainer(project, userMapper.map(user));
    }

    @WithMockUser(username = "user")
    @Test
    void hasUserPermission() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.hasPermission(project, READ, false)).isFalse();
        assertThat(securityACLService.hasPermission(project, READ)).isFalse();
        assertThat(securityACLService.hasPermission(project, READ, true)).isTrue();

        builder.addUserToProject(project, user.getUsername());

        assertThat(securityACLService.hasPermission(project, READ, false)).isTrue();
        assertThat(securityACLService.hasPermission(project, READ)).isTrue();
        assertThat(securityACLService.hasPermission(project, READ, true)).isTrue();
    }

    @WithMockUser(username = "user")
    @Test
    void hasRightToReadAbstractImage() {
        Project project = builder.givenAProject();
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.hasRightToReadAbstractImageWithProject(imageInstance.getBaseImage())).isFalse();

        builder.addUserToProject(project, user.getUsername());

        assertThat(securityACLService.hasRightToReadAbstractImageWithProject(imageInstance.getBaseImage())).isTrue();

    }

    @WithMockUser(username = "user")
    @Test
    void listAuthorizedProjects() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        UserResponse userResponse = userMapper.map(user);
        assertThat(securityACLService.getProjectList(userResponse, project.getOntology().getId())).doesNotContain(
            project);

        permissionService.addPermission(project, user.getUsername(), READ);

        assertThat(securityACLService.getProjectList(userResponse, project.getOntology().getId())).contains(project);

    }

    @WithMockUser(username = "user")
    @Test
    void listUserFromProjects() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();

        assertThat(securityACLService.getProjectUsers(project)).doesNotContain(user.getUsername());

        permissionService.addPermission(project, user.getUsername(), READ);

        assertThat(securityACLService.getProjectUsers(project)).contains(user.getUsername());

    }

    @WithMockUser(username = "user")
    @Test
    void checkSameUser() {
        User user = builder.givenDefaultUser();
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsSameUser(builder.givenSuperAdmin(), userMapper.map(user))
        );
        securityACLService.checkIsSameUser(userMapper.map(user).id(), userMapper.map(user));
        securityACLService.checkIsSameUser(user, userMapper.map(builder.givenSuperAdmin()));
    }

    @WithMockUser(username = "user")
    @Test
    void checkIsAdmin() {
        User user = builder.givenDefaultUser();
        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkAdmin(userMapper.map(user))
        );
        securityACLService.checkAdmin(userMapper.map(builder.givenSuperAdmin()));
    }

    @WithMockUser(username = "user")
    @Test
    void checkIsUser() {
        User user = builder.givenDefaultUser();
        User guest = builder.givenAGuest();

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkAdmin(userMapper.map(guest))
        );
        securityACLService.checkUser(userMapper.map(user));
        securityACLService.checkUser(userMapper.map(builder.givenSuperAdmin()));
    }

    @WithMockUser(username = "user")
    @Test
    void checkIsGuest() {
        User user = builder.givenDefaultUser();
        User guest = builder.givenAGuest();

        securityACLService.checkGuest(userMapper.map(guest));
        securityACLService.checkGuest(userMapper.map(user));
        securityACLService.checkGuest(userMapper.map(builder.givenSuperAdmin()));
    }

    @WithMockUser(username = "user")
    @Test
    void checkNotReadonly() {
        Project project = builder.givenAProject();
        User user = builder.givenDefaultUser();
        permissionService.addPermission(project, user.getUsername(), READ);

        securityACLService.checkIsNotReadOnly(project);

        project.setMode(EditingMode.READ_ONLY);

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> securityACLService.checkIsNotReadOnly(project)
        );

        permissionService.addPermission(project, user.getUsername(), ADMINISTRATION);

        securityACLService.checkIsNotReadOnly(project);
    }

    @WithMockUser(username = "superadmin")
    @Test
    void checkIsUserInProject() {
        Project project = builder.givenAProject();
        User user = builder.givenAUser();
        assertThat(securityACLService.isUserInProject(user, project)).isFalse();
        builder.addUserToProject(project, user.getUsername());
        assertThat(securityACLService.isUserInProject(user, project)).isTrue();
    }
}
