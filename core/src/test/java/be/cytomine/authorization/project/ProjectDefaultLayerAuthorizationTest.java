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
import be.cytomine.domain.project.ProjectDefaultLayer;
import be.cytomine.domain.security.User;
import be.cytomine.service.project.ProjectDefaultLayerService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ProjectDefaultLayerAuthorizationTest extends CRDAuthorizationTest {


    private ProjectDefaultLayer projectDefaultLayer = null;

    @Autowired
    ProjectDefaultLayerService projectDefaultLayerService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (projectDefaultLayer == null) {
            projectDefaultLayer = builder.givenAProjectDefaultLayer();
            initACL(projectDefaultLayer.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListProjectRepresentativeUser() {
        expectOK(() -> projectDefaultLayerService.listByProject(projectDefaultLayer.getProject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCanListProjectRepresentativeUser() {
        expectOK(() -> projectDefaultLayerService.listByProject(projectDefaultLayer.getProject()));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userNoAclCannotListProjectRepresentativeUser() {
        expectForbidden(() -> projectDefaultLayerService.listByProject(projectDefaultLayer.getProject()));
    }

    @Override
    public void whenIGetDomain() {
        projectDefaultLayerService.get(projectDefaultLayer.getId());
        projectDefaultLayerService.find(projectDefaultLayer.getId());
    }

    @Override
    protected void whenIAddDomain() {
        User user = builder.givenAUser();
        builder.addUserToProject(projectDefaultLayer.getProject(), user.getUsername());
        projectDefaultLayerService.add(
            builder.givenANotPersistedProjectRepresentativeUser(projectDefaultLayer.getProject(), user).toJsonObject()
        );
    }

    @Override
    protected void whenIDeleteDomain() {
        User user = projectDefaultLayer.getUser();
        builder.addUserToProject(projectDefaultLayer.getProject(), user.getUsername());
        ProjectDefaultLayer projectDefaultLayerToDelete = builder.givenANotPersistedProjectDefaultLayer(
            projectDefaultLayer.getProject(),
            user
        );
        builder.persistAndReturn(projectDefaultLayerToDelete);
        projectDefaultLayerService.delete(projectDefaultLayerToDelete, null, null, true);
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
