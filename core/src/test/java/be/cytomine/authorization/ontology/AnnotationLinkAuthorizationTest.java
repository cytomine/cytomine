package be.cytomine.authorization.ontology;

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
import be.cytomine.authorization.CRDAuthorizationTest;
import be.cytomine.domain.ontology.AnnotationLink;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.service.ontology.AnnotationLinkService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AnnotationLinkAuthorizationTest extends CRDAuthorizationTest {

    private AnnotationLink annotationLink = null;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    AnnotationLinkService annotationLinkService;

    @BeforeEach
    public void before() throws Exception {
        if (annotationLink == null) {
            annotationLink = builder.givenAnAnnotationLink();
            initACL(annotationLink.container());
        }
        annotationLink.getGroup().getProject().setMode(EditingMode.CLASSIC);
    }

    @Override
    protected void whenIGetDomain() {
        annotationLinkService.get(annotationLink.getId());
    }

    @Override
    protected void whenIAddDomain() {
        UserAnnotation annotation = builder.givenAUserAnnotation();
        annotation.setImage(annotationLink.getImage());
        annotation.setProject(annotationLink.getImage().getProject());

        annotationLinkService.add(builder.givenANotPersistedAnnotationLink(
            annotation, annotationLink.getGroup(), annotation.getImage()
        ).toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        annotationLinkService.delete(annotationLink, null, null, true);
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
    public void admin_can_list_annotation_group_by_annotation_group() {
        assertThat(annotationLinkService.list(annotationLink.getGroup())).contains(annotationLink);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_list_annotation_link_by_annotation_group() {
        assertThat(annotationLinkService.list(annotationLink.getGroup())).contains(annotationLink);
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_admin_can_add_in_readonly_mode() {
        annotationLink.getImage().getProject().setMode(EditingMode.READ_ONLY);
        expectOK(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_update_annotation_group_in_restricted_project() {
        AnnotationLink annotationLink = builder.givenAnAnnotationLink();
        annotationLink.getImage().getProject().setMode(EditingMode.RESTRICTED);
        expectOK(this::whenIGetDomain);
        expectOK(this::whenIAddDomain);
        expectOK(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_update_annotation_group_in_classic_project() {
        AnnotationLink annotationLink = builder.givenAnAnnotationLink();
        annotationLink.getImage().getProject().setMode(EditingMode.CLASSIC);
        expectOK(this::whenIGetDomain);
        expectOK(this::whenIAddDomain);
        expectOK(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_admin_can_delete_in_readonly_mode() {
        annotationLink.getImage().getProject().setMode(EditingMode.READ_ONLY);
        expectOK(this::whenIDeleteDomain);
    }
}
