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
import be.cytomine.domain.ontology.AnnotationTrack;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.service.ontology.AnnotationTrackService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AnnotationTrackAuthorizationTest extends CRDAuthorizationTest {

    private AnnotationTrack annotationTrack = null;

    @Autowired
    AnnotationTrackService annotationTrackService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (annotationTrack == null) {
            annotationTrack = builder.givenAnAnnotationTrack();
            initACL(annotationTrack.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_annotationTracks() {
        expectOK(() -> annotationTrackService.list(annotationTrack.getTrack()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_list_annotationTracks() {
        expectOK(() -> annotationTrackService.list(annotationTrack.getTrack()));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_without_acl_cannot_list_annotationTracks() {
        expectForbidden(() -> annotationTrackService.list(annotationTrack.getTrack()));
    }

    @Override
    public void whenIGetDomain() {
        annotationTrackService.get(annotationTrack.getId());
    }

    @Override
    protected void whenIAddDomain() {
        UserAnnotation annotation = builder.givenAUserAnnotation();
        annotation.setImage(annotationTrack.getTrack().getImage());
        annotation.setProject(annotationTrack.getTrack().getProject());

        annotationTrackService.add(
            builder.givenANotPersistedAnnotationTrack()
                .toJsonObject()
                .withChange("annotationIdent", annotation.getId())
                .withChange("track", this.annotationTrack.getTrack().getId()));
    }


    @Override
    protected void whenIDeleteDomain() {
        UserAnnotation annotation = builder.givenAUserAnnotation();
        annotation.setImage(annotationTrack.getTrack().getImage());
        annotation.setProject(annotationTrack.getTrack().getProject());

        AnnotationTrack annotationTrackToDelete = builder.givenAnAnnotationTrack();
        annotationTrackToDelete.setAnnotation(annotation);
        annotationTrackToDelete.setTrack(this.annotationTrack.getTrack());
        annotationTrackService.delete(annotationTrackToDelete, null, null, true);
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
