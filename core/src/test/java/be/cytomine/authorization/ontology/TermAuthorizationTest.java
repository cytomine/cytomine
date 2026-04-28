package be.cytomine.authorization.ontology;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.TermMapper;
import be.cytomine.authorization.CRDAuthorizationTest;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.ontology.Term;
import be.cytomine.service.ontology.TermService;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
@Import({WiremockRepository.class})
public class TermAuthorizationTest extends CRDAuthorizationTest {


    @Autowired
    TermService termService;
    @Autowired
    BasicInstanceBuilder builder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;
    @Autowired
    private TermMapper termMapper;
    private Term term = null;


    @SneakyThrows
    @BeforeEach
    public void beforeEach() throws ParseException {
        if (term == null) {
            term = builder.givenATerm();
            initACL(term.container());
        }


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
    public void guestAddDomain() {
    }

    @Test
    @Disabled
    @Override
    public void userWithoutPermissionGetDomain() {

    }

    @Test
    @Disabled
    @Override
    public void userWithoutPermissionAddDomain() {
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
