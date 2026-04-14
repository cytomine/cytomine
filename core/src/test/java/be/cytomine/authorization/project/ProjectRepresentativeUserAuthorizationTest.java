package be.cytomine.authorization.project;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRDAuthorizationTest;
import be.cytomine.domain.project.ProjectRepresentativeUser;
import be.cytomine.domain.security.User;
import be.cytomine.service.project.ProjectRepresentativeUserService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ProjectRepresentativeUserAuthorizationTest extends CRDAuthorizationTest {

    private ProjectRepresentativeUser projectRepresentativeUser = null;

    @Autowired
    ProjectRepresentativeUserService projectRepresentativeUserService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (projectRepresentativeUser == null) {
            projectRepresentativeUser = builder.givenAProjectRepresentativeUser();
            initACL(projectRepresentativeUser.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListProjectRepresentativeUser() {
        expectOK(() -> {
            projectRepresentativeUserService
                .listByProject(projectRepresentativeUser.getProject());
        });
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCanListProjectRepresentativeUser() {
        expectOK(() -> {
            projectRepresentativeUserService
                .listByProject(projectRepresentativeUser.getProject());
        });
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userNoAclCannotListProjectRepresentativeUser() {
        expectForbidden(() -> {
            projectRepresentativeUserService
                .listByProject(projectRepresentativeUser.getProject());
        });
    }

    @Override
    public void whenIGetDomain() {
        projectRepresentativeUserService.get(projectRepresentativeUser.getId());
        projectRepresentativeUserService.find(
            projectRepresentativeUser.getProject(), projectRepresentativeUser.getUser());
    }

    @Override
    protected void whenIAddDomain() {
        User user = builder.givenAUser();
        builder.addUserToProject(projectRepresentativeUser.getProject(), user.getUsername());
        projectRepresentativeUserService.add(
            builder.givenANotPersistedProjectRepresentativeUser(
                projectRepresentativeUser.getProject(), user
            ).toJsonObject()
        );
    }

    @Override
    protected void whenIDeleteDomain() {
        User user = projectRepresentativeUser.getUser();
        builder.addUserToProject(projectRepresentativeUser.getProject(), user.getUsername());
        ProjectRepresentativeUser
            projectRepresentativeUserToDelete
            = builder.givenANotPersistedProjectRepresentativeUser(
            projectRepresentativeUser.getProject(),
            user
        );
        builder.persistAndReturn(projectRepresentativeUserToDelete);
        projectRepresentativeUserService.delete(projectRepresentativeUserToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(BasePermission.WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(BasePermission.WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(BasePermission.WRITE);
    }


    @Override
    protected Optional<String> minimalRoleForCreate() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForDelete() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForEdit() {
        return Optional.of("ROLE_USER");
    }
}
