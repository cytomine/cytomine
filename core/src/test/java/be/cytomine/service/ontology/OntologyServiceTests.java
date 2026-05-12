package be.cytomine.service.ontology;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.repository.ontology.OntologyRepository;
import be.cytomine.repository.ontology.RelationTermRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.PermissionService;
import be.cytomine.service.command.TransactionService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class OntologyServiceTests {

    @Autowired
    private OntologyService ontologyService;

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private CommandService commandService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private RelationTermRepository relationTermRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private TermHttpContract termHttpContract;

    private Optional<Long> getTerm(Long termId) {
        String request = "select count(*) from term where id = :id and deleted is null";
        Query query = entityManager.createNativeQuery(request);
        query.setParameter("id", termId);
        long count = ((Number) query.getSingleResult()).longValue();
        return count > 0 ? Optional.of(termId) : Optional.empty();
    }

    @Test
    void listAllOntologyWithSuccess() {
        Ontology ontology = builder.givenAnOntology();
        assertThat(ontology).isIn(ontologyService.list());
    }

    @Test
    void getOntologyWithSuccess() {
        Ontology ontology = builder.givenAnOntology();
        assertThat(ontology).isEqualTo(ontologyService.get(ontology.getId()));
    }

    @Test
    void getUnexistingOntologyReturnNull() {
        assertThat(ontologyService.get(0L)).isNull();
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
    void addValidOntologyWithSuccess() {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();

        CommandResponse commandResponse = ontologyService.add(ontology.toJsonObject());

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(ontologyService.find(commandResponse.getObject().getId())).isPresent();
        Ontology created = ontologyService.find(commandResponse.getObject().getId()).get();
        assertThat(created.getName()).isEqualTo(ontology.getName());
    }

    @Test
    void addOntologyWithNullNameFail() {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        ontology.setName("");
        Assertions.assertThrows(WrongArgumentException.class, () -> ontologyService.add(ontology.toJsonObject()));
    }

    @Test
    void undoRedoOntologyCreationWithSuccess() {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        CommandResponse commandResponse = ontologyService.add(ontology.toJsonObject());
        assertThat(ontologyService.find(commandResponse.getObject().getId())).isPresent();

        commandService.undo();

        assertThat(ontologyService.find(commandResponse.getObject().getId())).isEmpty();

        var results = commandService.redo();
        assertThat(results.size()).isEqualTo(1);
        assertThat(ontologyService.find(results.get(0).getObject().getId())).isPresent();

    }

    @Test
    void redoOntologyCreationFailIfOntologyAlreadyExist() {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        CommandResponse commandResponse = ontologyService.add(ontology.toJsonObject());
        assertThat(ontologyService.find(commandResponse.getObject().getId())).isPresent();

        commandService.undo();

        assertThat(ontologyService.find(commandResponse.getObject().getId())).isEmpty();

        Ontology ontologyWithSameName = basicInstanceBuilder.givenANotPersistedOntology();
        ontologyWithSameName.setName(ontology.getName());
        builder.persistAndReturn(ontologyWithSameName);

        // re-create a ontology with a name that already exist in this ontology
        Assertions.assertThrows(AlreadyExistException.class, () -> commandService.redo());
    }

    @Test
    void editValidOntologyWithSuccess() {
        Ontology ontology = builder.givenAnOntology();

        CommandResponse commandResponse = ontologyService.update(
            ontology,
            ontology.toJsonObject().withChange("name", "NEW NAME")
        );

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(ontologyService.find(commandResponse.getObject().getId())).isPresent();
        Ontology edited = ontologyService.find(commandResponse.getObject().getId()).get();
        assertThat(edited.getName()).isEqualTo("NEW NAME");
    }

    @Test
    void undoRedoOntologyEditionWithSuccess() {
        Ontology ontology = builder.givenAnOntology();
        ontology.setName("OLD NAME");
        ontology = builder.persistAndReturn(ontology);

        ontologyService.update(ontology, ontology.toJsonObject().withChange("name", "NEW NAME"));

        assertThat(ontologyRepository.getById(ontology.getId()).getName()).isEqualTo("NEW NAME");

        commandService.undo();

        assertThat(ontologyRepository.getById(ontology.getId()).getName()).isEqualTo("OLD NAME");

        commandService.redo();

        assertThat(ontologyRepository.getById(ontology.getId()).getName()).isEqualTo("NEW NAME");

    }

    @Test
    void deleteOntologyWithSuccess() {
        Ontology ontology = builder.givenAnOntology();

        CommandResponse commandResponse = ontologyService.delete(ontology, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(ontologyService.find(ontology.getId()).isEmpty());
    }

    @Test
    void deleteOntologyWithDependenciesWithSuccess() {
        Ontology ontology = builder.givenAnOntology();
        Term term1 = builder.givenATerm(ontology);
        Term term2 = builder.givenATerm(ontology);
        builder.givenARelationTerm(term1, term2);

        CommandResponse commandResponse = ontologyService.delete(ontology, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(ontologyService.find(ontology.getId()).isEmpty());
    }


    @Test
    void undoRedoOntologyDeletionWithSuccess() {
        Ontology ontology = builder.givenAnOntology();

        ontologyService.delete(ontology, null, null, true);

        assertThat(ontologyService.find(ontology.getId()).isEmpty());

        commandService.undo();

        assertThat(ontologyService.find(ontology.getId()).isPresent());

        commandService.redo();

        assertThat(ontologyService.find(ontology.getId()).isEmpty());
    }

    @Test
    void undoRedoOntologyDeletionRestoreDependencies() {
        Ontology ontology = builder.givenAnOntology();
        Term term1 = builder.givenATerm(ontology);
        Term term2 = builder.givenATerm(ontology);
        RelationTerm relationTerm = builder.givenARelationTerm(term1, term2);

        ontologyService.delete(ontology, transactionService.start(), null, true);

        assertThat(ontologyService.find(ontology.getId()).isEmpty());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isEmpty();
        assertThat(getTerm(term1.getId())).isEmpty();
        assertThat(getTerm(term2.getId())).isEmpty();
        commandService.undo();

        assertThat(ontologyService.find(ontology.getId()).isPresent());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isPresent();
        assertThat(getTerm(term1.getId())).isPresent();
        assertThat(getTerm(term2.getId())).isPresent();

        commandService.redo();

        assertThat(ontologyService.find(ontology.getId()).isEmpty());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isEmpty();
        assertThat(getTerm(term1.getId())).isEmpty();
        assertThat(getTerm(term2.getId())).isEmpty();

        commandService.undo();

        assertThat(ontologyService.find(ontology.getId()).isPresent());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isPresent();
        assertThat(getTerm(term1.getId())).isPresent();
        assertThat(getTerm(term2.getId())).isPresent();

        commandService.redo();

        assertThat(ontologyService.find(ontology.getId()).isEmpty());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isEmpty();
        assertThat(getTerm(term1.getId())).isEmpty();
        assertThat(getTerm(term2.getId())).isEmpty();
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
    @WithMockUser("user")
    void determineRightsForUsersKeepRightsForOntologyCreator() {
        // create ontology for user
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        CommandResponse commandResponse = ontologyService.add(ontology.toJsonObject());
        ontology = (Ontology) commandResponse.getObject();

        assertThat(ontology.getUser().getUsername()).isEqualTo("user");
        assertThat(permissionService.hasACLPermission(ontology, "user", ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(ontology, "user", READ)).isTrue();

        // create project with ontology
        Project project = basicInstanceBuilder.givenANotPersistedProject();
        project.setOntology(ontology);
        commandResponse = projectService.add(project.toJsonObject());
        project = (Project) commandResponse.getObject();

        assertThat(ontology.getUser().getUsername()).isEqualTo("user");
        assertThat(permissionService.hasACLPermission(ontology, "user", ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(ontology, "user", READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, "user", ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(project, "user", READ)).isTrue();

        // change project ontology
        projectService.update(project, project.toJsonObject().withChange("ontology", null));

        // check that use still keep its rights to access ontology
        assertThat(ontology.getUser().getUsername()).isEqualTo("user");
        assertThat(permissionService.hasACLPermission(ontology, "user", ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(ontology, "user", READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, "user", ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(project, "user", READ)).isTrue();
    }
}
