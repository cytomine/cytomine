package be.cytomine.authorization.ontology;

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
import be.cytomine.domain.ontology.Term;
import be.cytomine.service.ontology.TermService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class TermAuthorizationTest extends CRDAuthorizationTest {
    @Autowired
    TermService termService;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;
    private Term term = null;

    @BeforeEach
    public void before() throws Exception {
        if (term == null) {
            term = builder.givenATerm();
            initACL(term.container());
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_terms() {
        expectOK(() -> termService.list());
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_list_terms() {
        expectForbidden(() -> termService.list());
    }

    @Override
    public void whenIGetDomain() {
        termService.get(term.getId());
    }

    @Override
    protected void whenIAddDomain() {
        termService.add(
            basicInstanceBuilder.givenANotPersistedTerm(term.getOntology()).toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        Term termToDelete = builder.givenATerm(term.getOntology());
        termService.delete(termToDelete, null, null, true);
    }

    @Test
    @Disabled
    @Override
    public void guest_add_domain() {
    }

    @Test
    @Disabled
    @Override
    public void user_without_permission_add_domain() {
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
        return Optional.empty();
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
        return Optional.empty();
    }
}
