package be.cytomine.authorization.image.group;

import java.util.Optional;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.service.image.group.ImageGroupService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ImageGroupAuthorizationTest extends CRUDAuthorizationTest {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private ImageGroupService imageGroupService;

    private ImageGroup imageGroup = null;

    @BeforeEach
    public void before() throws Exception {
        if (imageGroup == null) {
            imageGroup = builder.givenAnImageGroup();
            initACL(imageGroup.container());
        }
        imageGroup.getProject().setMode(EditingMode.CLASSIC);
        imageGroup.getProject().setAreImagesDownloadable(true);
    }

    @Override
    protected void whenIGetDomain() {
        imageGroupService.get(imageGroup.getId());
    }

    @Override
    protected void whenIAddDomain() {
        imageGroupService.add(builder.givenANotPersistedImagegroup(imageGroup.getProject()).toJsonObject());
    }

    @Override
    protected void whenIEditDomain() {
        imageGroupService.update(imageGroup, imageGroup.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        ImageGroup imageGroupToDelete = imageGroup;
        imageGroupService.delete(imageGroupToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(READ);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(READ);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(READ);
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

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListImagegroup() {
        assertThat(imageGroupService.list(imageGroup.getProject())).contains(imageGroup);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanListImagegroup() {
        assertThat(imageGroupService.list(imageGroup.getProject())).contains(imageGroup);
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userAdminCanAddInReadonlyMode() {
        imageGroup.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(() -> whenIAddDomain());
    }
}
