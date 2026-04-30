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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.TermMapper;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ConstraintException;
import be.cytomine.service.CommandService;
import be.cytomine.service.command.TransactionService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class TermServiceTests {

    @Autowired
    TermService termService;

    @Autowired
    BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;


    @Autowired
    TransactionService transactionService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TermMapper termMapper;

    @MockitoBean
    TermHttpContract termHttpContract;

    @MockitoBean
    TermRelationHttpContract termRelationHttpContract;

    @BeforeEach
    void setUp() {
        when(termRelationHttpContract.findTermRelationsByTermID(anyLong(), anyLong())).thenReturn(Set.of());
    }

    private Optional<Long> getTermRelation(Long termRelationId) {
        String request = "select count(*) from relation_term where id = :id and deleted is null";
        Query query = entityManager.createNativeQuery(request);
        query.setParameter("id", termRelationId);
        long count = ((Number) query.getSingleResult()).longValue();
        return count > 0 ? Optional.of(termRelationId) : Optional.empty();
    }

    @Test
    void getTermWithSuccess() {
        Term term = builder.givenATerm();
        when(termHttpContract.findTermByID(eq(term.getId()), anyLong())).thenReturn(Optional.of(termMapper.map(term)));
        assertEquals(term.getId(), termService.get(term.getId()).id());
    }

    @Test
    void getUnexistingTermReturnNull() {
        assertThat(termService.get(0L)).isNull();
    }

    @Test
    void findTermWithSuccess() {
        Term term = builder.givenATerm();
        when(termHttpContract.findTermByID(eq(term.getId()), anyLong())).thenReturn(Optional.of(termMapper.map(term)));
        assertTrue(termService.find(term.getId()).isPresent());
        assertEquals(term.getId(), termService.find(term.getId()).get().id());
    }

    @Test
    void findUnexistingTermReturnEmpty() {
        assertThat(termService.find(0L)).isEmpty();
    }

    @Test
    void listTermByOntologyIncludeTermFromOntology() {
        Term term = builder.givenATerm();
        when(termHttpContract.findAllTermIdsByOntology(eq(term.getOntology().getId()), anyLong()))
            .thenReturn(Set.of(term.getId()));
        assertThat(term.getId()).isIn(termService.list(term.getOntology()));
    }

    @Test
    void listTermByOntologyDoNotIncludeTermFromOtherOntology() {
        Term term = builder.givenATerm();
        Ontology ontology = builder.givenAnOntology();
        when(termHttpContract.findAllTermIdsByOntology(eq(ontology.getId()), anyLong()))
            .thenReturn(Set.of());
        assertThat(termService.list(ontology).size()).isEqualTo(0);
    }

    @Test
    void listTermByProjectIncludeTermFromProjectOntology() {
        Term term = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(term.getOntology());
        when(termHttpContract.findAllTermIdsByProject(eq(project.getId()), anyLong()))
            .thenReturn(Set.of(term.getId()));
        assertThat(term.getId()).isIn(termService.getAllTermIds(project));
    }

    @Test
    void listTermByProjectDoNotIncludeTermFromOtherOntology() {
        Term term = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(builder.givenAnOntology());
        when(termHttpContract.findAllTermIdsByProject(eq(project.getId()), anyLong()))
            .thenReturn(Set.of());
        assertEquals(new HashSet<>(), termService.getAllTermIds(project));
    }

    @Test
    void listTermByProjectReturnEmptyResultIfProjectHasNoOntology() {
        Project project = builder.givenAProjectWithOntology(null);
        when(termHttpContract.findAllTermIdsByProject(eq(project.getId()), anyLong()))
            .thenReturn(Set.of());
        assertEquals(new HashSet<>(), termService.getAllTermIds(project));
    }


    @Test
    void listTermIdsByProjectIncludeTermFromProjectOntology() {
        Term term = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(term.getOntology());
        when(termHttpContract.findAllTermIdsByProject(eq(project.getId()), anyLong()))
            .thenReturn(Set.of(term.getId()));

        assertThat(term.getId()).isIn(termService.getAllTermIds(project));
    }


    @Test
    void listTermIdsByProjectReturnEmptyResultIfProjectHasNoOntology() {
        Project project = builder.givenAProjectWithOntology(null);
        assertEquals(new HashSet<>(), termService.getAllTermIds(project));
    }

    @Test
    void deleteTermWithSuccess() {
        Term term = builder.givenATerm();

        Optional<HttpCommandResponse> commandResponse = termService.delete(term.getId());

        assertThat(commandResponse).isNotNull();
        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void deleteTermWithDependenciesWithSuccess() {
        Term term = builder.givenATerm();

        Optional<HttpCommandResponse> commandResponse = termService.delete(term.getId());

        assertThat(commandResponse).isNotNull();
        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void deleteTermWithAnnotationTermFails() {
        Term term = builder.givenATerm();
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm();
        annotationTerm.setTerm(term);

        Assertions.assertThrows(
            ConstraintException.class, () -> {
                termService.delete(term.getId());
            }
        );

        assertThat(entityManager.find(Term.class, term.getId())).isNotNull();
        assertThat(entityManager.find(AnnotationTerm.class, annotationTerm.getId())).isNotNull();
    }

    @Test
    void deleteTermWithReviewedAnnotationTermFails() {
        Term term = builder.givenATerm();
        ReviewedAnnotation reviewedAnnotation = builder.givenAReviewedAnnotation();
        reviewedAnnotation.getTerms().add(term);

        Assertions.assertThrows(
            ConstraintException.class, () -> {
                termService.delete(term.getId());
            }
        );

        assertThat(entityManager.find(Term.class, term.getId())).isNotNull();
        assertThat(entityManager.find(ReviewedAnnotation.class, reviewedAnnotation.getId())).isNotNull();
    }


    @Test
    void undoRedoTermDeletionWithSuccess() {
        Term term = builder.givenATerm();

        termService.delete(term.getId());

        assertThat(termService.find(term.getId()).isEmpty());

        commandService.undo();

        assertThat(termService.find(term.getId()).isPresent());

        commandService.redo();

        assertThat(termService.find(term.getId()).isEmpty());
    }
}
