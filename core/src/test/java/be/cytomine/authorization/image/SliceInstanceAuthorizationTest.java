package be.cytomine.authorization.image;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.service.image.SliceInstanceService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class SliceInstanceAuthorizationTest extends CRUDAuthorizationTest {

    // We need more flexibility:

    private SliceInstance sliceInstance = null;

    @Autowired
    SliceInstanceService sliceInstanceService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (sliceInstance == null) {
            sliceInstance = builder.givenASliceInstance();
            initACL(sliceInstance.container());
        }
        sliceInstance.getProject().setMode(EditingMode.CLASSIC);
        sliceInstance.getProject().setAreImagesDownloadable(true);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListSliceInstances() {
        assertThat(sliceInstanceService.list(sliceInstance.getImage())).contains(sliceInstance);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanListSliceInstances() {
        assertThat(sliceInstanceService.list(sliceInstance.getImage())).contains(sliceInstance);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotDeleteInRestrictedMode() {
        sliceInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> whenIDeleteDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userAdminCanAddInReadonlyMode() {
        sliceInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(() -> whenIAddDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userAdminCanEdiInReadonlyMode() {
        sliceInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(() -> whenIEditDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userAdminCanDeleteInReadonlyMode() {
        sliceInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(() -> whenIDeleteDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotAddInReadonlyMode() {
        sliceInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectForbidden(() -> whenIAddDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotEditInReadonlyMode() {
        sliceInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectForbidden(() -> whenIEditDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotDeleteInReadonlyMode() {
        sliceInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectForbidden(() -> whenIDeleteDomain());
    }


    @Override
    public void whenIGetDomain() {
        sliceInstanceService.get(sliceInstance.getId());
    }

    @Override
    protected void whenIAddDomain() {
        sliceInstanceService.add(
            builder.givenANotPersistedSliceInstance(
                builder.givenAnImageInstance(sliceInstance.getProject()),
                builder.givenAnAbstractSlice()
            ).toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        sliceInstanceService.update(sliceInstance, sliceInstance.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        SliceInstance sliceInstanceToDelete = sliceInstance;
        sliceInstanceService.delete(sliceInstanceToDelete, null, null, true);
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
}
