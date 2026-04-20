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

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ConstraintException;
import be.cytomine.repository.ontology.RelationTermRepository;
import be.cytomine.repository.ontology.TermRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.command.TransactionService;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class TermServiceTests {

    @Autowired
    TermService termService;

    @Autowired
    TermRepository termRepository;

    @Autowired
    BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;

    @Autowired
    RelationTermRepository relationTermRepository;

    @Autowired
    TransactionService transactionService;

    @Autowired
    EntityManager entityManager;

    @Test
    void listAllTermWithSuccess() {
        Term term = builder.givenATerm();
        assertThat(term).isIn(termService.list());
    }

    @Test
    void getTermWithSuccess() {
        Term term = builder.givenATerm();
        assertThat(term).isEqualTo(termService.get(term.getId()));
    }

    @Test
    void getUnexistingTermReturnNull() {
        assertThat(termService.get(0L)).isNull();
    }

    @Test
    void findTermWithSuccess() {
        Term term = builder.givenATerm();
        assertThat(termService.find(term.getId()).isPresent());
        assertThat(term).isEqualTo(termService.find(term.getId()).get());
    }

    @Test
    void findUnexistingTermReturnEmpty() {
        assertThat(termService.find(0L)).isEmpty();
    }

    @Test
    void listTermByOntologyIncludeTermFromOntology() {
        Term term = builder.givenATerm();
        assertThat(term).isIn(termService.list(term.getOntology()));
    }

    @Test
    void listTermByOntologyDoNotIncludeTermFromOtherOntology() {
        Term term = builder.givenATerm();
        Ontology ontology = builder.givenAnOntology();
        assertThat(termService.list(ontology).size()).isEqualTo(0);
    }

    @Test
    void listTermByProjectIncludeTermFromProjectOntology() {
        Term term = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(term.getOntology());
        assertThat(term).isIn(termService.list(project));
    }

    @Test
    void listTermByProjectDoNotIncludeTermFromOtherOntology() {
        Term term = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(builder.givenAnOntology());
        assertThat(termService.list(project)).asList().isEmpty();
    }

    @Test
    void listTermByProjectReturnEmptyResultIfProjectHasNoOntology() {
        Project project = builder.givenAProjectWithOntology(null);
        assertThat(termService.list(project)).asList().isEmpty();
    }


    @Test
    void listTermIdsByProjectIncludeTermFromProjectOntology() {
        Term term = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(term.getOntology());
        assertThat(term.getId()).isIn(termService.getAllTermId(project));
    }

    @Test
    void listTermIdsByProjectDoNotIncludeTermFromOtherOntology() {
        Term term = builder.givenATerm();
        Project project = builder.givenAProjectWithOntology(builder.givenAnOntology());
        assertThat(termService.getAllTermId(project)).asList().doesNotContain(term.getId());
    }

    @Test
    void listTermIdsByProjectReturnEmptyResultIfProjectHasNoOntology() {
        Project project = builder.givenAProjectWithOntology(null);
        assertThat(termService.getAllTermId(project)).asList().isEmpty();
    }

    @Test
    void deleteTermWithSuccess() {
        Term term = builder.givenATerm();

        CommandResponse commandResponse = termService.delete(term, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void deleteTermWithDependenciesWithSuccess() {
        Term term = builder.givenATerm();
        RelationTerm relationTerm = builder.givenARelationTerm(term, builder.givenATerm(term.getOntology()));

        CommandResponse commandResponse = termService.delete(term, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void deleteTermWithAnnotationTermFails() {
        Term term = builder.givenATerm();
        AnnotationTerm annotationTerm = builder.givenAnAnnotationTerm();
        annotationTerm.setTerm(term);

        Assertions.assertThrows(
            ConstraintException.class, () -> {
                termService.delete(term, null, null, true);
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
                termService.delete(term, null, null, true);
            }
        );

        assertThat(entityManager.find(Term.class, term.getId())).isNotNull();
        assertThat(entityManager.find(ReviewedAnnotation.class, reviewedAnnotation.getId())).isNotNull();
    }


    @Test
    void undoRedoTermDeletionWithSuccess() {
        Term term = builder.givenATerm();

        termService.delete(term, null, null, true);

        assertThat(termService.find(term.getId()).isEmpty());

        commandService.undo();

        assertThat(termService.find(term.getId()).isPresent());

        commandService.redo();

        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void undoRedoTermDeletionRestoreDependencies() {
        Term term = builder.givenATerm();
        RelationTerm relationTerm = builder.givenARelationTerm(term, builder.givenATerm(term.getOntology()));
        CommandResponse commandResponse = termService.delete(term, transactionService.start(), null, true);

        assertThat(termService.find(term.getId()).isEmpty());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isEmpty();

        commandService.undo();

        assertThat(termService.find(term.getId()).isPresent());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isPresent();

        commandService.redo();

        assertThat(termService.find(term.getId()).isEmpty());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isEmpty();

        commandService.undo();

        assertThat(termService.find(term.getId()).isPresent());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isPresent();

        commandService.redo();

        assertThat(termService.find(term.getId()).isEmpty());
        assertThat(relationTermRepository.findById(relationTerm.getId())).isEmpty();
    }
}
