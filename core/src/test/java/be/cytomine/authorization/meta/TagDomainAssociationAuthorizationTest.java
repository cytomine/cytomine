package be.cytomine.authorization.meta;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.meta.TagDomainAssociation;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.service.meta.TagDomainAssociationService;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class TagDomainAssociationAuthorizationTest extends CRDAuthorizationTest {
    private TagDomainAssociation tagDomainAssociationForProject = null;
    private TagDomainAssociation tagDomainAssociationForAnnotation = null;
    private TagDomainAssociation tagDomainAssociationForAbstractImage = null;

    private Project project = null;
    private AnnotationDomain annotationDomain = null;
    private AbstractImage abstractImage = null;

    @Autowired
    TagDomainAssociationService tagDomainAssociationService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (tagDomainAssociationForProject == null) {
            project = builder.givenAProject();
            annotationDomain = builder.givenAUserAnnotation();
            abstractImage = builder.givenAnAbstractImage();

            tagDomainAssociationForProject = builder.givenATagAssociation(builder.givenATag(), project);
            tagDomainAssociationForAnnotation = builder.givenATagAssociation(
                builder.givenATag(),
                annotationDomain
            );
            tagDomainAssociationForAbstractImage = builder.givenATagAssociation(
                builder.givenATag(),
                abstractImage
            );

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
        expectOK(() -> tagDomainAssociationService.listAllByDomain(project));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanListWithFilters() {
        expectOK(() -> tagDomainAssociationService.listAllByDomain(project));
        assertThat(tagDomainAssociationService.list(new ArrayList<>()))
            .contains(
                tagDomainAssociationForProject,
                tagDomainAssociationForAnnotation,
                tagDomainAssociationForAbstractImage
            );
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutAclCannotListWithFilters() {
        assertThat(tagDomainAssociationService.list(new ArrayList<>()))
            .doesNotContain(tagDomainAssociationForProject, tagDomainAssociationForAnnotation)
            .contains(tagDomainAssociationForAbstractImage);
    }

    @Override
    public void whenIGetDomain() {}

    @Override
    protected void whenIAddDomain() {}

    @Test
    @Override
    @Disabled
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutPermissionGetDomain() {}

    @Override
    protected void whenIDeleteDomain() {
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(
            builder.givenATag(),
            annotationDomain
        );
        tagDomainAssociationService.delete(tagDomainAssociation, null, null, true);
    }





    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanDeleteForImage() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(
            builder.givenATag(),
            imageInstance
        );
        expectOK(() -> tagDomainAssociationService.delete(tagDomainAssociation, null, null, true));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCannotDeleteInRestrictedModeForImage() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        expectForbidden(() -> tagDomainAssociationService.delete(
            builder.givenATagAssociation(builder.givenATag(), imageInstance), null, null, true
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userCanDeleteInRestrictedModeForImageIfOwner() {
        ImageInstance imageInstance = builder.givenAnImageInstance(project);
        imageInstance.getProject().setMode(EditingMode.RESTRICTED);
        imageInstance.setUser(userRepository.findByUsernameLikeIgnoreCase(USER_ACL_READ).get());
        expectOK(() -> tagDomainAssociationService.delete(
            builder.givenATagAssociation(builder.givenATag(), imageInstance), null, null, true
        ));
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCannotDeleteForImage() {
        expectForbidden(() -> tagDomainAssociationService.delete(
            builder.givenATagAssociation(builder.givenATag(), builder.givenAnImageInstance()), null, null, true
        ));
    }




    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanDeleteForProject() {
        expectOK(() -> tagDomainAssociationService.delete(
            builder.givenATagAssociation(
                builder.givenATag(),
                project
            ), null, null, true
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadCannotDeleteForProject() {
        expectForbidden(() -> tagDomainAssociationService.delete(
            builder.givenATagAssociation(
                builder.givenATag(),
                project
            ), null, null, true
        ));
    }

    @Test
    @WithMockUser(username = USER_ACL_WRITE)
    public void userWithWriteCanDeleteForProject() {
        expectOK(() -> tagDomainAssociationService.delete(
            builder.givenATagAssociation(
                builder.givenATag(),
                project
            ), null, null, true
        ));
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
