package be.cytomine.authorization.ontology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.AbstractAuthorizationTest;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.service.ontology.OntologyService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class OntologyAuthorizationTest extends AbstractAuthorizationTest {

    @MockitoBean
    TermHttpContract termHttpContract;
    @Autowired
    OntologyService ontologyService;
    @Autowired
    BasicInstanceBuilder builder;
    private Ontology ontology = null;

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
}
