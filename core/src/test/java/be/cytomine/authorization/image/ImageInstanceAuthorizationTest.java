package be.cytomine.authorization.image;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
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
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.service.image.ImageInstanceService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class ImageInstanceAuthorizationTest extends CRUDAuthorizationTest {

    private ImageInstance imageInstance = null;

    @Autowired
    ImageInstanceService imageInstanceService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (imageInstance == null) {
            imageInstance = builder.givenAnImageInstance();
            initACL(imageInstance.container());
        }
        imageInstance.getProject().setMode(EditingMode.CLASSIC);
        imageInstance.getProject().setAreImagesDownloadable(true);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListImageInstances() {
        assertThat(imageInstanceService.listByProject(imageInstance.getProject())).contains(imageInstance);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanListImageInstances() {
        assertThat(imageInstanceService.listByProject(imageInstance.getProject())).contains(imageInstance);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotDeleteInRestrictedMode() {
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> whenIDeleteDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userAdminCanAddInReadonlyMode() {
        imageInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(() -> whenIAddDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userAdminCanEdiInReadonlyMode() {
        imageInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(() -> whenIEditDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userAdminCanDeleteInReadonlyMode() {
        imageInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(() -> whenIDeleteDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotAddInReadonlyMode() {
        imageInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectForbidden(() -> whenIAddDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotEditInReadonlyMode() {
        imageInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectForbidden(() -> whenIEditDomain());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotDeleteInReadonlyMode() {
        imageInstance.getProject().setMode(EditingMode.READ_ONLY);
        expectForbidden(() -> whenIDeleteDomain());
    }


    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userCannotStopReviewStartedBySomeoneElse() {
        ImageInstance imageInstance = builder.givenAnImageInstance();
        imageInstance.setProject(this.imageInstance.getProject());
        imageInstance.setReviewStart(new Date());
        imageInstance.setReviewUser(builder.givenSuperAdmin());
        Assertions.assertThrows(
            WrongArgumentException.class, () -> {
                imageInstanceService.stopReview(imageInstance, false);
            }
        );
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void userCanStopReviewStartedByHimself() {
        ImageInstance imageInstance = builder.givenAnImageInstance();
        imageInstance.setProject(this.imageInstance.getProject());
        imageInstance.setReviewStart(new Date());
        imageInstance.setReviewUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_ADMIN).get());
        imageInstanceService.stopReview(imageInstance, false);
    }

    @Override
    public void whenIGetDomain() {
        imageInstanceService.get(imageInstance.getId());
    }

    @Override
    protected void whenIAddDomain() {
        imageInstanceService.add(builder.givenANotPersistedImageInstance(imageInstance.getProject())
            .toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        imageInstanceService.update(imageInstance, imageInstance.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        ImageInstance imageInstanceToDelete = imageInstance;
        imageInstanceService.delete(imageInstanceToDelete, null, null, true);
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
