package org.cytomine.repository.http;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.ReviewedAnnotationLinkRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.ReviewedAnnotationLinkEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.CurrentUserService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.ReviewedAnnotationHttpContract;

import static be.cytomine.common.repository.http.ReviewedAnnotationHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class ReviewedAnnotationController implements ReviewedAnnotationHttpContract {
    private final ReviewedAnnotationLinkRepository reviewedAnnotationLinkRepository;
    private final ACLService aclService;
    private final TermRepository termRepository;
    private final CurrentUserService currentUserService;

    @Override
    @PutMapping("/terms/{reviewedAnnotationTermsId}")
    @Transactional
    public Set<Long> replaceAllTermIds(long reviewedAnnotationTermsId, Set<Long> newLinks) {

        long userId = currentUserService.getCurrentUserId();
        if (!termRepository.findAllById(newLinks)
            .stream()
            .map(TermEntity::getOntologyId)
            .distinct()
            .allMatch(ontologyId -> aclService.canWriteOntology(userId, ontologyId))) {
            return Set.of();
        }

        Set<ReviewedAnnotationLinkEntity> existing =
            reviewedAnnotationLinkRepository.findAllByReviewedAnnotationTermsId(reviewedAnnotationTermsId);
        if (existing.stream()
            .map(ReviewedAnnotationLinkEntity::getTermId)
            .collect(Collectors.toSet())
            .equals(newLinks)) {
            return existing.stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet());
        } else {
            reviewedAnnotationLinkRepository.deleteAllByReviewedAnnotationTermsId(reviewedAnnotationTermsId);
            return reviewedAnnotationLinkRepository.saveAll(newLinks.stream()
                .map(termId -> new ReviewedAnnotationLinkEntity(termId, reviewedAnnotationTermsId))
                .toList()).stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet());
        }
    }
}
