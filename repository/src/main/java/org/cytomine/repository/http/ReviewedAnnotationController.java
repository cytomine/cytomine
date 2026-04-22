package org.cytomine.repository.http;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.ReviewedAnnotationLinkRepository;
import org.cytomine.repository.persistence.entity.ReviewedAnnotationLinkEntity;
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

    @Override
    @PutMapping("/terms/{reviewedAnnotationTermsId}")
    @Transactional
    public Set<Long> replaceAllTermIds(long reviewedAnnotationTermsId, long userId, Set<Long> newLinks) {
        Set<ReviewedAnnotationLinkEntity> existing =
            reviewedAnnotationLinkRepository.findAllByReviewedAnnotationTermsId(reviewedAnnotationTermsId);
        if (!existing.stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet())
            .equals(newLinks)) {
            return existing.stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet());
        } else {
            reviewedAnnotationLinkRepository.deleteAllByReviewedAnnotationTermsId(reviewedAnnotationTermsId);
            return reviewedAnnotationLinkRepository.saveAll(
                newLinks.stream().map(termId -> new ReviewedAnnotationLinkEntity(reviewedAnnotationTermsId, termId))
                    .toList()).stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet());
        }
    }
}
