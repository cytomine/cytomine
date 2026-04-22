package be.cytomine.service.ontology;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.config.MongoTestConfiguration;
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

import static be.cytomine.service.search.RetrievalService.CBIR_API_BASE_PATH;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class OntologyServiceTests {

    private static WireMockServer wireMockServer;
    @Autowired
    OntologyService ontologyService;
    @Autowired
    OntologyRepository ontologyRepository;
    @Autowired
    BasicInstanceBuilder basicInstanceBuilder;
    @Autowired
    BasicInstanceBuilder builder;
    @Autowired
    CommandService commandService;
    @Autowired
    TransactionService transactionService;
    @Autowired
    RelationTermRepository relationTermRepository;
    @MockitoBean
    TermHttpContract termHttpContract;
    @Autowired
    PermissionService permissionService;
    @Autowired
    ProjectService projectService;
    @Autowired
    EntityManager entityManager;

    private static void setupStub() {
        /* Simulate call to CBIR */
        wireMockServer.stubFor(post(urlPathEqualTo(CBIR_API_BASE_PATH + "/storages"))
            .withRequestBody(matching(".*"))
            .willReturn(aResponse().withBody(UUID.randomUUID().toString()))
        );
    }

    @BeforeAll
    public static void beforeAll() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        setupStub();
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    private Optional<TermResponse> getTerm(Long termId) {
        String request = "select * from term where id = :id and deleted = null";
        Query query = entityManager.createNativeQuery(request, Tuple.class);
        query.setParameter("id", termId);
        return Optional.ofNullable(query.getResultList().get(0))
            .map(TermResponse.class::cast);
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
        Assertions.assertThrows(
            WrongArgumentException.class, () -> {
                ontologyService.add(ontology.toJsonObject());
            }
        );
    }


    @Test
    void undoRedoOntologyCreationWithSuccess() {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        CommandResponse commandResponse = ontologyService.add(ontology.toJsonObject());
        assertThat(ontologyService.find(commandResponse.getObject().getId())).isPresent();
        System.out.println(
            "id = " + commandResponse.getObject().getId() + " name = " + ontology.getName());

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
        System.out.println(
            "id = " + commandResponse.getObject().getId() + " name = " + ontology.getName());

        commandService.undo();

        assertThat(ontologyService.find(commandResponse.getObject().getId())).isEmpty();

        Ontology ontologyWithSameName = basicInstanceBuilder.givenANotPersistedOntology();
        ontologyWithSameName.setName(ontology.getName());
        builder.persistAndReturn(ontologyWithSameName);

        // re-create a ontology with a name that already exist in this ontology
        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                commandService.redo();
            }
        );
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
        RelationTerm relationTerm = builder.givenARelationTerm(term1, term2);

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

        CommandResponse commandResponse =
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
            ontology, userAdminInProject.getUsername(),
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
        commandResponse = projectService.update(
            project, project.toJsonObject()
                .withChange("ontology", null)
        );

        // check that use still keep its rights to access ontology
        assertThat(ontology.getUser().getUsername()).isEqualTo("user");
        assertThat(permissionService.hasACLPermission(ontology, "user", ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(ontology, "user", READ)).isTrue();
        assertThat(permissionService.hasACLPermission(project, "user", ADMINISTRATION)).isTrue();
        assertThat(permissionService.hasACLPermission(project, "user", READ)).isTrue();
    }
}
