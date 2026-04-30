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
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.ConstraintException;
import be.cytomine.repository.ontology.AnnotationTermRepository;
import be.cytomine.repository.ontology.ReviewedAnnotationRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.security.SecurityACLService;

@Slf4j
@Service
@Transactional
public class TermService {

    @Autowired
    private SecurityACLService securityACLService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TermRelationHttpContract relationTermService;

    @Autowired
    private AnnotationTermRepository annotationTermRepository;

    @Autowired
    private TermHttpContract termHttpContract;

    @Autowired
    private ReviewedAnnotationRepository reviewedAnnotationRepository;

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

    public Optional<HttpCommandResponse> delete(Long termId) {

        verifyDeleteDependentRelationTerm(termId);
        verifyDeleteAnnotationTerm(termId);
        verifyDeleteReviewedAnnotationTerm(termId);

        return termHttpContract.delete(termId, currentUserService.getCurrentUser().getId());
    }


    public List<String> getStringParamsI18n(CytomineDomain domain) {
        Term term = (Term) domain;
        return Arrays.asList(String.valueOf(term.getId()), term.getName(), term.getOntology().getName());
    }

    private void verifyDeleteDependentRelationTerm(Long termId) {
        relationTermService.findTermRelationsByTermID(termId, currentUserService.getCurrentUser().getId())
            .forEach(trr -> relationTermService.delete(trr.id(), currentUserService.getCurrentUser().getId()));
    }

    private void verifyDeleteAnnotationTerm(Long termId) {
        long terms = annotationTermRepository.countByTermId(termId);
        if (terms != 0) {
            throw new ConstraintException(
                "Term is still linked with " + (terms) + " annotations created by user. Cannot delete term!");
        }
    }

    private void verifyDeleteReviewedAnnotationTerm(Long termId) {
        long terms = reviewedAnnotationRepository.countAllByTermsId(termId);
        if (terms != 0) {
            throw new ConstraintException(
                "Term is still linked with " + (terms) + " reviewed annotations. Cannot delete term!");
        }
    }
}
