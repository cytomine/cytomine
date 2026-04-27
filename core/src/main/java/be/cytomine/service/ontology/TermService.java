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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.command.Command;
import be.cytomine.domain.command.DeleteCommand;
import be.cytomine.domain.command.Transaction;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.ConstraintException;
import be.cytomine.repository.ontology.AnnotationTermRepository;
import be.cytomine.repository.ontology.ReviewedAnnotationRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.ModelService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

import static org.springframework.security.acls.domain.BasePermission.DELETE;

@Slf4j
@Service
@Transactional
public class TermService extends ModelService {

    @Autowired
    private SecurityACLService securityACLService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private RelationTermService relationTermService;

    @Autowired
    private AnnotationTermRepository annotationTermRepository;

    @Autowired
    private TermHttpContract termHttpContract;

    @Autowired
    private ReviewedAnnotationRepository reviewedAnnotationRepository;

    @Override
    public Class currentDomain() {
        return Term.class;
    }

    public TermResponse get(Long id) {
        return find(id).orElse(null);
    }

    public Optional<TermResponse> find(Long id) {
        return termHttpContract.findTermByID(id, currentUserService.getCurrentUser().getId());
    }


    public Set<Long> list(Ontology ontology) {
        return termHttpContract.findAllTermIdsByOntology(ontology.getId(), currentUserService.getCurrentUser().getId());
    }

    public Set<Long> getAllTermIds(Project project) {
        return termHttpContract.findAllTermIdsByProject(project.getId(), currentUserService.getCurrentUser().getId());
    }

    public String fillEmptyTermIds(String terms, Project project) {
        if (terms == null || terms.isEmpty()) {
            return this.getAllTermIds(project).stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        return terms;
    }

    /**
     * Delete this domain
     *
     * @param domain       Domain to delete
     * @param transaction  Transaction link with this command
     * @param task         Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    @Override
    public CommandResponse delete(CytomineDomain domain, Transaction transaction, Task task, boolean printMessage) {
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        securityACLService.check(domain.container(), DELETE);
        Command c = new DeleteCommand(currentUser, transaction);
        return executeCommand(c, domain, null);
    }


    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new Term().buildDomainFromJson(json, getEntityManager());
    }

    @Override
    public List<String> getStringParamsI18n(CytomineDomain domain) {
        Term term = (Term) domain;
        return Arrays.asList(String.valueOf(term.getId()), term.getName(), term.getOntology().getName());
    }

    @Override
    public void deleteDependencies(CytomineDomain domain, Transaction transaction, Task task) {
        deleteDependentRelationTerm((Term) domain, transaction, task);
        deleteAnnotationTerm((Term) domain, transaction, task);
        deleteReviewedAnnotationTerm((Term) domain, transaction, task);
    }

    public void deleteDependentRelationTerm(Term term, Transaction transaction, Task task) {
        for (RelationTerm relationTerm : relationTermService.list(term)) {
            relationTermService.delete(relationTerm, transaction, task, false);
        }
    }

    public void deleteAnnotationTerm(Term term, Transaction transaction, Task task) {
        long terms = annotationTermRepository.countByTerm(term);
        if (terms != 0) {
            throw new ConstraintException(
                "Term is still linked with " + (terms) + " annotations created by user. Cannot delete term!");
        }
    }

    public void deleteReviewedAnnotationTerm(Term term, Transaction transaction, Task task) {
        long terms = reviewedAnnotationRepository.countAllByTermsContaining(term);
        if (terms != 0) {
            throw new ConstraintException(
                "Term is still linked with " + (terms) + " reviewed annotations. Cannot delete term!");
        }
    }
}
