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
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.service.ontology.RelationTermService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class RelationTermAuthorizationTest extends CRDAuthorizationTest {

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    private RelationTerm relationTerm = null;

    @Autowired
    RelationTermService relationTermService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (relationTerm == null) {
            relationTerm = builder.givenARelationTerm();
            initACL(relationTerm.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_relation_terms() {
        expectOK(() -> relationTermService.list(relationTerm.getTerm1()));
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_with_read_can_list_relation_terms() {
        expectOK(() -> relationTermService.list(relationTerm.getTerm1()));
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void user_no_acl_cannot_list_relation_terms() {
        expectForbidden(() -> relationTermService.list(relationTerm.getTerm1()));
    }


    @Override
    public void when_i_get_domain() {
        relationTermService.find(relationTerm.getRelation(), relationTerm.getTerm1(), relationTerm.getTerm2());
    }

    @Override
    protected void when_i_add_domain() {
        relationTermService.add(
            basicInstanceBuilder.givenANotPersistedRelationTerm(
                relationTerm.getRelation(),
                relationTerm.getTerm1(),
                builder.givenATerm(relationTerm.getTerm1().getOntology())
            ).toJsonObject()
        );
    }

    @Override
    protected void when_i_delete_domain() {
        RelationTerm termToDelete = builder.givenARelationTerm(
            relationTerm.getRelation(),
            relationTerm.getTerm1(),
            builder.givenATerm(relationTerm.getTerm1().getOntology())
        );
        relationTermService.delete(termToDelete, null, null, true);
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
