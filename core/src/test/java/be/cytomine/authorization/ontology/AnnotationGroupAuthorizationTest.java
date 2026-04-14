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
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.service.ontology.AnnotationGroupService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.READ;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AnnotationGroupAuthorizationTest extends CRUDAuthorizationTest {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private AnnotationGroupService annotationGroupService;

    private AnnotationGroup annotationGroup = null;

    @BeforeEach
    public void before() throws Exception {
        if (annotationGroup == null) {
            annotationGroup = builder.givenAnAnnotationGroup();
            initACL(annotationGroup.container());
        }
        annotationGroup.getProject().setMode(EditingMode.CLASSIC);
        annotationGroup.getProject().setAreImagesDownloadable(true);
    }

    @Override
    protected void whenIGetDomain() {
        annotationGroupService.get(annotationGroup.getId());
    }

    @Override
    protected void whenIAddDomain() {
        annotationGroupService.add(builder.givenANotPersistedAnnotationGroup(
            annotationGroup.getProject(), annotationGroup.getImageGroup()).toJsonObject()
        );
    }

    @Override
    protected void when_i_edit_domain() {
        annotationGroupService.update(annotationGroup, annotationGroup.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        annotationGroupService.delete(annotationGroup, null, null, true);
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
    public void admin_can_list_annotation_group_by_project() {
        assertThat(annotationGroupService.list(annotationGroup.getProject())).contains(annotationGroup);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_list_annotation_group_by_project() {
        assertThat(annotationGroupService.list(annotationGroup.getProject())).contains(annotationGroup);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_annotation_group_by_image_group() {
        assertThat(annotationGroupService.list(annotationGroup.getImageGroup())).contains(annotationGroup);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_list_annotation_group_by_image_group() {
        assertThat(annotationGroupService.list(annotationGroup.getImageGroup())).contains(annotationGroup);
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_admin_can_add_in_readonly_mode() {
        annotationGroup.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_update_annotation_group_in_restricted_project() {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        annotationGroup.getProject().setMode(EditingMode.RESTRICTED);
        expectOK(this::whenIGetDomain);
        expectOK(this::whenIAddDomain);
        expectOK(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_update_annotation_group_in_classic_project() {
        AnnotationGroup annotationGroup = builder.givenAnAnnotationGroup();
        annotationGroup.getProject().setMode(EditingMode.CLASSIC);
        expectOK(this::whenIGetDomain);
        expectOK(this::whenIAddDomain);
        expectOK(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_ADMIN)
    public void user_admin_can_delete_in_readonly_mode() {
        annotationGroup.getProject().setMode(EditingMode.READ_ONLY);
        expectOK(this::whenIDeleteDomain);
    }
}
