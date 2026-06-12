package be.cytomine.service.ontology;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.ontology.OntologyExport;
import be.cytomine.service.PermissionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class OntologyServiceTests {

    @MockitoBean
    TermHttpContract termHttpContract;
    @Autowired
    private OntologyService ontologyService;
    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;
    @Autowired
    private BasicInstanceBuilder builder;
    @Autowired
    private PermissionService permissionService;

    @Test
    void listAllOntologyWithSuccess() {
        Ontology ontology = builder.givenAnOntology();
        assertThat(ontology).isIn(ontologyService.list());
    }

    @Test
    void findOntologyWithSuccess() {
        Ontology ontology = builder.givenAnOntology();
        assertThat(ontologyService.find(ontology.getId()).isPresent());
        assertThat(ontology).isEqualTo(ontologyService.find(ontology.getId()).get());
    }

    @Test
    void findUnexistingOntologyReturnEmpty() {
        assertThat(ontologyService.find(0L)).isEmpty();
    }

    @Test
    void listLightOntology() {
        Ontology ontology = builder.givenAnOntology();
        assertThat(ontologyService.listLight()
            .stream()
            .anyMatch(json -> json.get("id").equals(ontology.getId()))).isTrue();
    }

    @Test
    void determineRightsForUsersAdminInProject() {
        Ontology ontology = builder.givenAnOntology();
        Project project = builder.givenAProjectWithOntology(ontology);
        User userAdminInProject = builder.givenAUser();
        User userNotAdminInProject = builder.givenAUser();
        User userNotInProject = builder.givenAUser();

        permissionService.addPermission(project, userAdminInProject.getUsername(), ADMINISTRATION);
        permissionService.addPermission(project, userNotAdminInProject.getUsername(), WRITE);

        ontologyService.determineRightsForUsers(
            ontology,
            List.of(userAdminInProject, userNotAdminInProject, userNotInProject)
        );

        assertThat(permissionService.hasACLPermission(
            ontology,
            userAdminInProject.getUsername(),
            ADMINISTRATION
        )).isTrue();

        assertThat(permissionService.hasACLPermission(
            ontology, userNotAdminInProject.getUsername(),
            ADMINISTRATION
        )).isFalse();
        assertThat(permissionService.hasACLPermission(
            ontology, userNotAdminInProject.getUsername(),
            READ
        )).isTrue();

        assertThat(permissionService.hasACLPermission(
            ontology, userNotInProject.getUsername(),
            ADMINISTRATION
        )).isFalse();
        assertThat(permissionService.hasACLPermission(
            ontology, userNotInProject.getUsername(),
            READ
        )).isFalse();
    }

    @Test
    public void exportShouldReturnOntologyExportWithMappedTerms() {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        Term term = basicInstanceBuilder.givenATerm(ontology);
        List<TermResponse> termResponses = List.of(
            new TermResponse(
                term.getId(), term.getName(), term.getColor(), term.getOntology().getId(),
                LocalDateTime.ofInstant(term.getCreated().toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(term.getUpdated().toInstant(), ZoneId.systemDefault()),
                Optional.empty(), Optional.ofNullable(term.getComment()), Set.of()
            )
        );

        when(termHttpContract.findTermsByOntology(eq(ontology.getId()), anyLong(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(termResponses));

        OntologyExport result = ontologyService.export(ontology);

        assertThat(result.name()).isEqualTo(ontology.getName());
        assertThat(result.terms()).hasSize(termResponses.size());
        assertThat(result.terms().getFirst().name()).isEqualTo(term.getName());
        assertThat(result.terms().getFirst().color()).isEqualTo(term.getColor());
    }
}
