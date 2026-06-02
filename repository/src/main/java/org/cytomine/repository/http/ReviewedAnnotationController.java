package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.ReviewedAnnotationMapper;
import org.cytomine.repository.persistence.ReviewedAnnotationLinkRepository;
import org.cytomine.repository.persistence.ReviewedAnnotationRepository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.ReviewedAnnotationLinkEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.ReviewedAnnotationCommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.ReviewedAnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.ReviewedAnnotationResponse;
import be.cytomine.common.repository.model.reviewedannotation.payload.CreateReviewedAnnotation;
import be.cytomine.common.repository.model.reviewedannotation.payload.UpdateReviewedAnnotation;

import static be.cytomine.common.repository.http.ReviewedAnnotationHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)

public class ReviewedAnnotationController implements ReviewedAnnotationHttpContract {
    private final ReviewedAnnotationLinkRepository reviewedAnnotationLinkRepository;
    private final ReviewedAnnotationRepository reviewedAnnotationRepository;
    private final ReviewedAnnotationCommandService reviewedAnnotationCommandService;
    private final ReviewedAnnotationMapper reviewedAnnotationMapper;
    private final ACLService aclService;
    private final TermRepository termRepository;

    @Override
    @GetMapping("/{id}")
    public Optional<ReviewedAnnotationResponse> findById(long id, long userId) {
        return reviewedAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(e -> reviewedAnnotationMapper.mapToResponse(e, reviewedAnnotationRepository.findTermIds(e.getId())));
    }

    @Override
    @GetMapping("/term/{termId}/count")
    public long countByTerm(long termId, long userId) {
        return reviewedAnnotationRepository.countByTermId(termId);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateReviewedAnnotation createReviewedAnnotation) {
        return reviewedAnnotationCommandService.createReviewedAnnotation(userId, createReviewedAnnotation,
            LocalDateTime.now());
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId,
        UpdateReviewedAnnotation updateReviewedAnnotation) {
        return reviewedAnnotationCommandService.updateReviewedAnnotation(id, userId, updateReviewedAnnotation,
            LocalDateTime.now());
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return reviewedAnnotationCommandService.deleteReviewedAnnotation(id, userId, LocalDateTime.now());
    }

    @Override
    @PutMapping("/terms/{reviewedAnnotationTermsId}")
    @Transactional
    public Set<Long> replaceAllTermIds(long reviewedAnnotationTermsId, long userId, Set<Long> newLinks) {

        if (!termRepository.findAllById(newLinks).stream().map(TermEntity::getOntologyId).distinct()
            .allMatch(ontologyId -> aclService.canWriteOntology(userId, ontologyId))) {
            return Set.of();
        }

        Set<ReviewedAnnotationLinkEntity> existing =
            reviewedAnnotationLinkRepository.findAllByReviewedAnnotationTermsId(reviewedAnnotationTermsId);
        if (existing.stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet())
            .equals(newLinks)) {
            return existing.stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet());
        } else {
            reviewedAnnotationLinkRepository.deleteAllByReviewedAnnotationTermsId(reviewedAnnotationTermsId);
            return reviewedAnnotationLinkRepository.saveAll(
                newLinks.stream().map(termId -> new ReviewedAnnotationLinkEntity(termId, reviewedAnnotationTermsId))
                    .toList()).stream().map(ReviewedAnnotationLinkEntity::getTermId).collect(Collectors.toSet());
        }
    }
}
