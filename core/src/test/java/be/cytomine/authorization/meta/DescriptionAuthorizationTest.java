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
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.meta.Description;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.service.meta.DescriptionService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class DescriptionAuthorizationTest extends CRUDAuthorizationTest {


    private Description descriptionForProject = null;
    private Description descriptionForAnnotation = null;
    private Description descriptionForAbstractImage = null;

    private Description descriptionForImageInstance = null;

    private Project project = null;
    private AnnotationDomain annotationDomain = null;
    private AbstractImage abstractImage = null;

    private ImageInstance imageInstance = null;


    @Autowired
    DescriptionService descriptionService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (descriptionForProject == null) {
            project = builder.givenAProject();
            annotationDomain = builder.givenAUserAnnotation();
            abstractImage = builder.givenAnAbstractImage();
            imageInstance = builder.givenAnImageInstance(project);

            descriptionForProject = builder.givenADescription(project);
            descriptionForAnnotation = builder.givenADescription(annotationDomain);
            descriptionForAbstractImage = builder.givenADescription(abstractImage);
            descriptionForImageInstance = builder.givenADescription(imageInstance);

            initACL(project);
            initACL(annotationDomain.getProject());
            initACL(abstractImage.getUploadedFile().getStorage());
        }
        project.setMode(EditingMode.CLASSIC);
        annotationDomain.getProject().setMode(EditingMode.CLASSIC);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list() {
        expectOK(() -> descriptionService.list());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_list() {
        expectForbidden(() -> descriptionService.list());
    }


    // ANNOTATIONS
    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_add_in_readonly_mode() {
        project.setMode(EditingMode.READ_ONLY);
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_add_in_restricted_mode() {
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_add_in_restricted_mode_for_annotation() {
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_canadd_in_restricted_mode_for_annotation_if_owner() {
        annotationDomain.getProject().setMode(EditingMode.RESTRICTED);
        ((UserAnnotation) annotationDomain).setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(this::whenIAddDomain);
    }

    @Override
    public void whenIGetDomain() {
        descriptionService.findByDomain(project);
        descriptionService.findByDomain("AnnotationDomain", annotationDomain.getId());
        descriptionService.findByDomain(abstractImage);
    }

    @Override
    protected void whenIAddDomain() {
        AnnotationDomain annotationDomain = builder.persistAndReturn(
            builder.givenANotPersistedUserAnnotation(project)
        );
        descriptionService.add(builder.givenANotPersistedDescription(annotationDomain).toJsonObject());
    }

    @Override
    protected void when_i_edit_domain() {
        descriptionService.update(descriptionForAnnotation, descriptionForAnnotation.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        Description description = builder.givenADescription(annotationDomain);
        descriptionService.delete(description, null, null, true);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_add_for_image() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(imageInstance).toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_add_in_restricted_mode_for_image() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(imageInstance)
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_add_in_restricted_mode_for_image_if_owner() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        imageInstance.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(imageInstance).toJsonObject()));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guest_cannot_add_image() {
        expectForbidden(() -> descriptionService.add(
            builder.givenANotPersistedDescription(builder.givenAnImageInstance()).toJsonObject())
        );
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_edit_for_image() {
        expectOK(() -> descriptionService.update(
            descriptionForImageInstance,
            descriptionForImageInstance.toJsonObject(),
            null,
            null
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_edit_in_restricted_mode_for_image() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> descriptionService.update(
            builder.givenADescription(imageInstance),
            builder.givenADescription(imageInstance).toJsonObject(),
            null
        ));

    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_edit_in_restricted_mode_for_image_if_owner() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        imageInstance.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        Description descriptionForImage = builder.givenADescription(imageInstance);
        expectOK(() -> descriptionService.update(descriptionForImage, descriptionForImage.toJsonObject(), null));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guest_cannot_edit_image() {
        Description descriptionForImage = builder.givenADescription(imageInstance);
        expectForbidden(() -> descriptionService.update(descriptionForImage, descriptionForImage.toJsonObject(), null));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_delete_for_image() {
        expectOK(() -> descriptionService.delete(descriptionForImageInstance, null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_delete_in_restricted_mode_for_image() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> descriptionService.delete(builder.givenADescription(imageInstance), null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_delete_in_restricted_mode_for_image_if_owner() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        imageInstance.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(() -> descriptionService.delete(builder.givenADescription(imageInstance), null, null, true));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guest_cannot_delete_for_image() {
        expectForbidden(() -> descriptionService.delete(descriptionForImageInstance, null, null, true));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_add_for_project() {
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(builder.givenAProject())
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_cannot_add_for_project() {
        expectForbidden(() -> descriptionService.add(builder.givenANotPersistedDescription(builder.givenAProject())
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void user_with_write_can_add_for_project() {
        Project projectLocal = builder.givenAProject();
        initACL(projectLocal);
        expectOK(() -> descriptionService.add(builder.givenANotPersistedDescription(projectLocal).toJsonObject()));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_edit_for_project() {
        expectOK(() -> descriptionService.update(
            descriptionForProject,
            descriptionForProject.toJsonObject(),
            null,
            null
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_cannot_edit_for_project() {
        expectForbidden(() -> descriptionService.update(
            descriptionForProject,
            descriptionForProject.toJsonObject(),
            null,
            null
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void user_with_write_can_edit_for_project() {
        Project projectLocal = builder.givenAProject();
        initACL(projectLocal);
        Description description = builder.givenADescription(projectLocal);
        expectOK(() -> descriptionService.update(description, description.toJsonObject(), null));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_delete_for_project() {
        expectOK(() -> descriptionService.delete(
            builder.givenADescription(builder.givenAProject()),
            null,
            null,
            true
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_cannot_delete_for_project() {
        expectForbidden(() -> descriptionService.delete(
            builder.givenADescription(builder.givenAProject()),
            null,
            null,
            true
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void user_with_write_can_delete_for_project() {
        Project projectLocal = builder.givenAProject();
        initACL(projectLocal);
        expectOK(() -> descriptionService.delete(builder.givenADescription(projectLocal), null, null, true));
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.of(BasePermission.READ);
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(BasePermission.READ);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(BasePermission.READ);
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
