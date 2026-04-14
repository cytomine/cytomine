package be.cytomine.authorization.processing;

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
import be.cytomine.domain.processing.ImageFilterProject;
import be.cytomine.service.processing.ImageFilterProjectService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ImageFilterProjectAuthorizationTest extends CRDAuthorizationTest {

    private ImageFilterProject imageFilterProject = null;

    @Autowired
    ImageFilterProjectService imageFilterProjectService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (imageFilterProject == null) {
            imageFilterProject = builder.givenAnImageFilterProject();
            initACL(imageFilterProject.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_all_image_filters() {
        expectOK(() -> imageFilterProjectService.list());
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_no_acl_cannot_list_project_representative_user() {
        expectForbidden(() -> imageFilterProjectService.list());
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void project_admin_can_list_project_image_filters() {
        expectOK(() -> imageFilterProjectService.list(imageFilterProject.getProject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void contributor_can_list_project_image_filter() {
        expectOK(() -> imageFilterProjectService.list(imageFilterProject.getProject()));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_not_in_project_cannot_list_project_image_filter() {
        expectForbidden(() -> imageFilterProjectService.list(imageFilterProject.getProject()));
    }

    @Override
    public void whenIGetDomain() {
        imageFilterProjectService.find(imageFilterProject.getImageFilter(), imageFilterProject.getProject());
    }

    @Override
    protected void whenIAddDomain() {
        imageFilterProjectService.add(
            builder.givenANotPersistedImageFilterProject(
                builder.givenAnImageFilter(),
                imageFilterProject.getProject()
            ).toJsonObject()
        );
    }

    @Override
    protected void whenIDeleteDomain() {
        ImageFilterProject imageFilterProjectThatMustBeDeleted = builder.givenANotPersistedImageFilterProject(
            builder.givenAnImageFilter(),
            imageFilterProject.getProject()
        );
        builder.persistAndReturn(imageFilterProjectThatMustBeDeleted);
        imageFilterProjectService.delete(imageFilterProjectThatMustBeDeleted, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(BasePermission.ADMINISTRATION);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(BasePermission.ADMINISTRATION);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(BasePermission.ADMINISTRATION);
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
