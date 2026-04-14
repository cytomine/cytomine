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
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.service.ontology.OntologyService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class OntologyAuthorizationTest extends CRUDAuthorizationTest {

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    private Ontology ontology = null;

    @Autowired
    OntologyService ontologyService;

    @Autowired
    BasicInstanceBuilder builder;

    @BeforeEach
    public void before() throws Exception {
        if (ontology == null) {
            ontology = builder.givenAnOntology();
            initACL(ontology);
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanListOntologies() {
        assertThat(ontologyService.list()).contains(ontology);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithAtLeastReadPermissionCanListOntologies() {
        assertThat(ontologyService.list()).contains(ontology);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userNoAclCannotListOntologies() {
        assertThat(ontologyService.list()).doesNotContain(ontology);
    }

    @Override
    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userWithoutPermissionAddDomain() {
        expectOK(() -> whenIAddDomain());
        // User with no ACL can create an ontology
    }

    @Override
    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void userWithReadPermissionAddDomain() {
        expectOK(() -> whenIAddDomain());
        // User with READ permission can create another ontology
    }

    @Override
    public void whenIGetDomain() {
        ontologyService.get(ontology.getId());
    }

    @Override
    protected void whenIAddDomain() {
        ontologyService.add(basicInstanceBuilder.givenANotPersistedOntology().toJsonObject());
    }

    @Override
    public void whenIEditDomain() {
        ontologyService.update(ontology, ontology.toJsonObject());
    }

    @Override
    protected void whenIDeleteDomain() {
        Ontology ontologyToDelete = ontology;
        ontologyService.delete(ontologyToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.empty();
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
