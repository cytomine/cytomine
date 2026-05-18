package be.cytomine.service.ontology;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Service
@Transactional
public class TermService {

    private final AnnotationTermRepository annotationTermRepository;

    private final CurrentUserService currentUserService;

    private final TermRelationHttpContract relationTermService;

    private final ReviewedAnnotationRepository reviewedAnnotationRepository;

    private final SecurityACLService securityACLService;

    private final TermHttpContract termHttpContract;

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
        relationTermService.findTermRelationsIdsByTermId(termId, currentUserService.getCurrentUser().getId())
            .forEach(trr -> relationTermService.delete(trr, currentUserService.getCurrentUser().getId()));
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
