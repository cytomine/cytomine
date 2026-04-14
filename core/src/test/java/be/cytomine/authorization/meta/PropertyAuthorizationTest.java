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
import be.cytomine.domain.meta.Property;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.service.meta.PropertyService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class PropertyAuthorizationTest extends CRUDAuthorizationTest {


    private Property propertyForProject = null;
    private Property propertyForAnnotation = null;
    private Property propertyForAbstractImage = null;
    private Property propertyForImageInstance = null;

    private Project project = null;
    private AnnotationDomain annotationDomain = null;
    private AbstractImage abstractImage = null;
    private ImageInstance imageInstance = null;

    @Autowired
    PropertyService propertyService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (propertyForProject == null) {
            project = builder.givenAProject();
            annotationDomain = builder.givenAUserAnnotation();
            abstractImage = builder.givenAnAbstractImage();
            imageInstance = builder.givenAnImageInstance(project);

            propertyForProject = builder.givenAProperty(project);
            propertyForAnnotation = builder.givenAProperty(annotationDomain);
            propertyForAbstractImage = builder.givenAProperty(abstractImage);
            propertyForImageInstance = builder.givenAProperty(imageInstance);

            initACL(project);
            initACL(annotationDomain.getProject());
            initACL(abstractImage.getUploadedFile().getStorage());
        }
        project.setMode(EditingMode.CLASSIC);
        annotationDomain.getProject().setMode(EditingMode.CLASSIC);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanList() {
        expectOK(() -> propertyService.list());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotList() {
        expectForbidden(() -> propertyService.list());
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutReadCannotListForDomain() {
        expectForbidden(() -> propertyService.list(abstractImage));
        expectForbidden(() -> propertyService.list(imageInstance));
        expectForbidden(() -> propertyService.list(project));
        expectForbidden(() -> propertyService.list(annotationDomain));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotAddInReadonlyMode() {
        project.setMode(EditingMode.READ_ONLY);
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotAddInRestrictedMode() {
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotAddInRestrictedModeForAnnotation() {
        project.setMode(EditingMode.RESTRICTED);
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanAddInRestrictedModeForAnnotationIfOwner() {
        project.setMode(EditingMode.RESTRICTED);
        ((UserAnnotation) annotationDomain).setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(() -> {
            AnnotationDomain annotationDomain = builder.persistAndReturn(builder.givenANotPersistedUserAnnotation(
                project));
            ((UserAnnotation) annotationDomain).setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ)
                .get());
            propertyService.add(builder.givenANotPersistedProperty(annotationDomain, "key", "value")
                .toJsonObject());
        });
    }

    @Override
    public void whenIGetDomain() {
        propertyService.findByDomainAndKey(project, "key");
        propertyService.findByDomainAndKey(annotationDomain, "key");
        propertyService.findByDomainAndKey(abstractImage, "key");
    }

    @Override
    protected void whenIAddDomain() {
        AnnotationDomain annotationDomain = builder.persistAndReturn(builder.givenANotPersistedUserAnnotation(
            project));
        propertyService.add(builder.givenANotPersistedProperty(annotationDomain, "key", "value").toJsonObject());
    }

    @Override
    protected void whenIEditDomain() {
        propertyService.update(propertyForAnnotation, propertyForAnnotation.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        Property property = builder.givenAProperty(annotationDomain);
        propertyService.delete(property, null, null, true);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanAddForImage() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(imageInstance, "key", "value")
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotAddInRestrictedModeForImage() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(imageInstance, "key", "value")
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanAddInRestrictedModeForImageIfOwner() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        imageInstance.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(imageInstance, "key", "value")
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCannotAddForImage() {
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(
            builder.givenAnImageInstance(),
            "key",
            "value"
        ).toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanEditForImage() {
        expectOK(() -> propertyService.update(
            propertyForImageInstance,
            propertyForImageInstance.toJsonObject(),
            null,
            null
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotEditInRestrictedModeForImage() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> propertyService.update(
            builder.givenAProperty(imageInstance),
            builder.givenAProperty(imageInstance).toJsonObject(),
            null
        ));

    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanEditInRestrictedModeForImageIfOwner() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        imageInstance.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        Property propertyForImageInstance = builder.givenAProperty(imageInstance);
        expectOK(() -> propertyService.update(propertyForImageInstance, propertyForImageInstance.toJsonObject(), null));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCannotEditImage() {
        Property propertyForImageInstanceLocal = builder.givenAProperty(imageInstance);
        expectForbidden(() -> propertyService.update(
            propertyForImageInstanceLocal,
            propertyForImageInstanceLocal.toJsonObject(),
            null
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanDeleteForImage() {
        expectOK(() -> propertyService.delete(propertyForImageInstance, null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotDeleteInRestrictedModeForImage() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> propertyService.delete(builder.givenAProperty(imageInstance), null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanDeleteInRestrictedModeForImageIfOwner() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        imageInstance.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(() -> propertyService.delete(builder.givenAProperty(imageInstance), null, null, true));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCannotDeleteForImage() {
        expectForbidden(() -> propertyService.delete(propertyForImageInstance, null, null, true));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanAddForProject() {
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(
            builder.givenAProject(),
            "key",
            "value"
        ).toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCannotAddForProject() {
        expectForbidden(() -> propertyService.add(builder.givenANotPersistedProperty(
            builder.givenAProject(),
            "key",
            "value"
        ).toJsonObject()));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWriteCanAddForProject() {
        Project projectLocal = builder.givenAProject();
        initACL(projectLocal);
        expectOK(() -> propertyService.add(builder.givenANotPersistedProperty(projectLocal, "key", "value")
            .toJsonObject()));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanEditForProject() {
        expectOK(() -> propertyService.update(propertyForProject, propertyForProject.toJsonObject(), null, null));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCannotEditForProject() {
        expectForbidden(() -> propertyService.update(
            propertyForProject,
            propertyForProject.toJsonObject(),
            null,
            null
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWriteCanEditForProject() {
        Project projectLocal = builder.givenAProject();
        initACL(projectLocal);
        Property property = builder.givenAProperty(projectLocal);
        expectOK(() -> propertyService.update(property, property.toJsonObject(), null));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanDeleteForProject() {
        expectOK(() -> propertyService.delete(builder.givenAProperty(builder.givenAProject()), null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCannotDeleteForProject() {
        expectForbidden(() -> propertyService.delete(
            builder.givenAProperty(builder.givenAProject()),
            null,
            null,
            true
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWriteCanDeleteForProject() {
        Project projectLocal = builder.givenAProject();
        initACL(projectLocal);
        expectOK(() -> propertyService.delete(builder.givenAProperty(projectLocal), null, null, true));
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
