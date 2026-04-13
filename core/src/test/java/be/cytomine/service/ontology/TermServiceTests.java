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
    void list_all_term_with_success() {
        Term term = builder.given_a_term();
        assertThat(term).isIn(termService.list());
    }

    @Test
    void get_term_with_success() {
        Term term = builder.given_a_term();
        assertThat(term).isEqualTo(termService.get(term.getId()));
    }

    @Test
    void get_unexisting_term_return_null() {
        assertThat(termService.get(0L)).isNull();
    }

    @Test
    void find_term_with_success() {
        Term term = builder.given_a_term();
        assertThat(termService.find(term.getId()).isPresent());
        assertThat(term).isEqualTo(termService.find(term.getId()).get());
    }

    @Test
    void find_unexisting_term_return_empty() {
        assertThat(termService.find(0L)).isEmpty();
    }

    @Test
    void list_term_by_ontology_include_term_from_ontology() {
        Term term = builder.given_a_term();
        assertThat(term).isIn(termService.list(term.getOntology()));
    }

    @Test
    void list_term_by_ontology_do_not_include_term_from_other_ontology() {
        Term term = builder.given_a_term();
        Ontology ontology = builder.given_an_ontology();
        assertThat(termService.list(ontology).size()).isEqualTo(0);
    }

    @Test
    void list_term_by_project_include_term_from_project_ontology() {
        Term term = builder.given_a_term();
        Project project = builder.given_a_project_with_ontology(term.getOntology());
        assertThat(term).isIn(termService.list(project));
    }

    @Test
    void list_term_by_project_do_not_include_term_from_other_ontology() {
        Term term = builder.given_a_term();
        Project project = builder.given_a_project_with_ontology(builder.given_an_ontology());
        assertThat(termService.list(project)).asList().isEmpty();
    }

    @Test
    void list_term_by_project_return_empty_result_if_project_has_no_ontology() {
        Project project = builder.given_a_project_with_ontology(null);
        assertThat(termService.list(project)).asList().isEmpty();
    }


    @Test
    void list_term_ids_by_project_include_term_from_project_ontology() {
        Term term = builder.given_a_term();
        Project project = builder.given_a_project_with_ontology(term.getOntology());
        assertThat(term.getId()).isIn(termService.getAllTermId(project));
    }

    @Test
    void list_term_ids_by_project_do_not_include_term_from_other_ontology() {
        Term term = builder.given_a_term();
        Project project = builder.given_a_project_with_ontology(builder.given_an_ontology());
        assertThat(termService.getAllTermId(project)).asList().doesNotContain(term.getId());
    }

    @Test
    void list_term_ids_by_project_return_empty_result_if_project_has_no_ontology() {
        Project project = builder.given_a_project_with_ontology(null);
        assertThat(termService.getAllTermId(project)).asList().isEmpty();
    }

    @Test
    void delete_term_with_success() {
        Term term = builder.given_a_term();

        CommandResponse commandResponse = termService.delete(term, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void delete_term_with_dependencies_with_success() {
        Term term = builder.given_a_term();
        RelationTerm relationTerm = builder.given_a_relation_term(term, builder.given_a_term(term.getOntology()));

        CommandResponse commandResponse = termService.delete(term, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void delete_term_with_annotation_term_fails() {
        Term term = builder.given_a_term();
        AnnotationTerm annotationTerm = builder.given_an_annotation_term();
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
    void delete_term_with_reviewed_annotation_term_fails() {
        Term term = builder.given_a_term();
        ReviewedAnnotation reviewedAnnotation = builder.given_a_reviewed_annotation();
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
    void undo_redo_term_deletion_with_success() {
        Term term = builder.given_a_term();

        termService.delete(term, null, null, true);

        assertThat(termService.find(term.getId()).isEmpty());

        commandService.undo();

        assertThat(termService.find(term.getId()).isPresent());

        commandService.redo();

        assertThat(termService.find(term.getId()).isEmpty());
    }

    @Test
    void undo_redo_term_deletion_restore_dependencies() {
        Term term = builder.given_a_term();
        RelationTerm relationTerm = builder.given_a_relation_term(term, builder.given_a_term(term.getOntology()));
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
