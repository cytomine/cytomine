package be.cytomine.authorization.meta;

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
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.meta.AttachedFile;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.service.meta.AttachedFileService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AttachedFileAuthorizationTest extends CRDAuthorizationTest {


    private AttachedFile attachedFile = null;

    private Project project = null;

    private AnnotationDomain attachedFileAnnotation = null;

    @Autowired
    AttachedFileService attachedFileService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (attachedFile == null) {
            attachedFileAnnotation = builder.givenAUserAnnotation();
            project = attachedFileAnnotation.getProject();
            attachedFile = builder.givenAnAttachedFile(attachedFileAnnotation);
            initACL(attachedFileAnnotation.container());
        }
        project.setMode(EditingMode.CLASSIC);
    }

    @Override
    @Test
    @WithMockUser(username = USER_ACL_CREATE)
    public void userWithCreatePermissionDeleteDomain() {
        expectOK(this::whenIDeleteDomain);
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIDeleteDomain);
        project.setMode(EditingMode.READ_ONLY);
        expectForbidden(this::whenIDeleteDomain);
    }

    @Override
    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadPermissionAddDomain() {
        expectOK(this::whenIAddDomain);
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIAddDomain);
        project.setMode(EditingMode.READ_ONLY);
        expectForbidden(this::whenIAddDomain);
    }

    @Override
    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadPermissionDeleteDomain() {
        expectOK(this::whenIDeleteDomain);
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIDeleteDomain);
        project.setMode(EditingMode.READ_ONLY);
        expectForbidden(this::whenIDeleteDomain);
    }

    @Override
    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWritePermissionDeleteDomain() {
        expectOK(this::whenIDeleteDomain);
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIDeleteDomain);
        project.setMode(EditingMode.READ_ONLY);
        expectForbidden(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutPermissionAddDomain() {
        expectForbidden(this::whenIAddDomain);
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIAddDomain);
        project.setMode(EditingMode.READ_ONLY);
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListByDomain() {
        expectOK(() -> {
            attachedFileService.findAllByDomain(attachedFile.getDomainClassName(), attachedFile.getDomainIdent());
        });
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanListByDomain() {
        expectOK(() -> {
            attachedFileService.findAllByDomain(attachedFile.getDomainClassName(), attachedFile.getDomainIdent());
        });
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userNotAclCannotListByDomain() {
        expectForbidden(() -> {
            attachedFileService.findAllByDomain(attachedFile.getDomainClassName(), attachedFile.getDomainIdent());
        });
    }

    // ANNOTATIONS
    @Override
    public void whenIGetDomain() {
        attachedFileService.findById(attachedFile.getId());
    }

    @Override
    protected void whenIAddDomain() {
        try {
            attachedFileService.create(
                "test",
                "hello".getBytes(),
                "test",
                attachedFileAnnotation.getId(),
                attachedFileAnnotation.getClass().getName()
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void whenIDeleteDomain() {
        AttachedFile attachedFile = builder.givenAnAttachedFile(attachedFileAnnotation);
        attachedFileService.delete(attachedFile, null, null, true);
    }

    //IMAGE

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanAddForImage() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        expectOK(() -> attachedFileService.create(
            "test",
            "hello".getBytes(),
            "test",
            attachedFileImage.getId(),
            attachedFileImage.getClass().getName()
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotAddInRestrictedModeForImage() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        attachedFileImage.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> attachedFileService.create(
            "test",
            "hello".getBytes(),
            "test",
            attachedFileImage.getId(),
            attachedFileImage.getClass().getName()
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanAddInRestrictedModeForImageIfOwner() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        attachedFileImage.getProject().setMode(EditingMode.RESTRICTED);
        attachedFileImage.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(() -> attachedFileService.create(
            "test",
            "hello".getBytes(),
            "test",
            attachedFileImage.getId(),
            attachedFileImage.getClass().getName()
        ));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCannotAddForImage() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        expectForbidden(() -> attachedFileService.create(
            "test",
            "hello".getBytes(),
            "test",
            attachedFileImage.getId(),
            attachedFileImage.getClass().getName()
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanDeleteForImage() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        AttachedFile attachedFile = builder.givenAnAttachedFile(attachedFileImage);
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotDeleteInRestrictedModeForImage() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        attachedFileImage.getProject().setMode(EditingMode.RESTRICTED);
        AttachedFile attachedFile = builder.givenAnAttachedFile(attachedFileImage);
        expectForbidden(() -> attachedFileService.delete(attachedFile, null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanDeleteInRestrictedModeForImageIfOwner() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        attachedFileImage.getProject().setMode(EditingMode.RESTRICTED);
        attachedFileImage.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        AttachedFile attachedFile = builder.givenAnAttachedFile(attachedFileImage);
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, true));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCannotDeleteForImage() {
        ImageInstance attachedFileImage = builder.givenAnImageInstance(project);
        AttachedFile attachedFile = builder.givenAnAttachedFile(attachedFileImage);
        expectForbidden(() -> attachedFileService.delete(attachedFile, null, null, true));
    }

    //PROJECT
    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanAddForProject() {
        expectOK(() -> attachedFileService.create(
            "test",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCannotAddForProject() {
        expectForbidden(() -> attachedFileService.create(
            "test",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWriteCanAddForProject() {
        expectOK(() -> attachedFileService.create(
            "test",
            "hello".getBytes(),
            "test",
            project.getId(),
            project.getClass().getName()
        ));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanDeleteForProject() {
        AttachedFile attachedFile = builder.givenAnAttachedFile(project);
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCannotDeleteForProject() {

        AttachedFile attachedFile = builder.givenAnAttachedFile(project);
        expectForbidden(() -> attachedFileService.delete(attachedFile, null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWriteCanDeleteForProject() {
        AttachedFile attachedFile = builder.givenAnAttachedFile(project);
        expectOK(() -> attachedFileService.delete(attachedFile, null, null, true));
    }


    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(BasePermission.WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(BasePermission.DELETE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(BasePermission.WRITE);
    }


    @Override
    protected Optional<String> minimalRoleForCreate() {
        return Optional.of("ROLE_GUEST");
    }

    @Override
    protected Optional<String> minimalRoleForDelete() {
        return Optional.of("ROLE_GUEST");
    }

    @Override
    protected Optional<String> minimalRoleForEdit() {
        return Optional.of("ROLE_GUEST");
    }
}
