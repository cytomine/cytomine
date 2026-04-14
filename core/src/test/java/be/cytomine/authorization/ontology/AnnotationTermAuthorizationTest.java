package be.cytomine.authorization.ontology;

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
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.project.EditingMode;
import be.cytomine.domain.project.Project;
import be.cytomine.service.ontology.AnnotationTermService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AnnotationTermAuthorizationTest extends CRDAuthorizationTest {

    private AnnotationTerm annotationTerm = null;

    @Autowired
    AnnotationTermService annotationTermService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (annotationTerm == null) {
            annotationTerm = builder.givenAnAnnotationTerm();
            initACL(annotationTerm.container());
        }
        annotationTerm.getUserAnnotation().getProject().setMode(EditingMode.CLASSIC);
        builder.persistAndReturn(annotationTerm.getUserAnnotation().getProject());
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_relation_terms() {
        expectOK(() -> annotationTermService.list(annotationTerm.getUserAnnotation()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_can_list_relation_terms() {
        expectOK(() -> annotationTermService.list(annotationTerm.getUserAnnotation()));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_no_acl_cannot_list_relation_terms() {
        expectForbidden(() -> annotationTermService.list(annotationTerm.getUserAnnotation()));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_update_annotation_in_restricted_project() {
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(this.annotationTerm.getUserAnnotation());
        Project project = (Project) annotationTerm.container();
        project.setMode(EditingMode.RESTRICTED);
        builder.persistAndReturn(project);
        expectOK(this::whenIGetDomain);
        expectOK(this::whenIAddDomain);
        expectOK(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_can_update_annotation_in_classic_project() {
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(this.annotationTerm.getUserAnnotation());
        Project project = (Project) annotationTerm.container();
        project.setMode(EditingMode.CLASSIC);
        builder.persistAndReturn(project);
        expectOK(this::whenIGetDomain);
        expectOK(this::whenIAddDomain);
        expectOK(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_update_annotation_in_readonly_project() {
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(this.annotationTerm.getUserAnnotation());
        Project project = (Project) annotationTerm.container();
        project.setMode(EditingMode.READ_ONLY);
        builder.persistAndReturn(project);
        expectOK(this::whenIGetDomain);
        expectForbidden(this::whenIDeleteDomain);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_update_annotation_in_restricted_project() {
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm(this.annotationTerm.getUserAnnotation());
        Project project = (Project) annotationTerm.container();
        project.setMode(EditingMode.RESTRICTED);
        builder.persistAndReturn(project);
        expectOK(this::whenIGetDomain);
        expectForbidden(this::whenIDeleteDomain);
    }

    @Override
    public void whenIGetDomain() {
        annotationTermService.find(
            annotationTerm.getUserAnnotation(),
            annotationTerm.getTerm(), annotationTerm.getUser()
        );
    }

    @Override
    protected void whenIAddDomain() {
        annotationTermService.add(
            builder.givenANotPersistedAnnotationTerm(annotationTerm.getUserAnnotation()).toJsonObject()
        );
    }

    @Override
    protected void whenIDeleteDomain() {
        AnnotationTerm annotationTerm = builder.givenANotPersistedAnnotationTerm(
            this.annotationTerm.getUserAnnotation()
        );
        builder.persistAndReturn(annotationTerm);
        annotationTermService.delete(annotationTerm, null, null, true);
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
