package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.ReviewedAnnotationMapper;
import org.cytomine.repository.persistence.ReviewedAnnotationRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.ReviewedAnnotationCommandService;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final ReviewedAnnotationRepository reviewedAnnotationRepository;
    private final ReviewedAnnotationCommandService reviewedAnnotationCommandService;
    private final ReviewedAnnotationMapper reviewedAnnotationMapper;
    private final ACLService aclService;

    @Override
    @GetMapping("/{id}")
    public Optional<ReviewedAnnotationResponse> findById(long id, long userId) {
        return reviewedAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(e -> reviewedAnnotationMapper.mapToResponse(e, getTermIds(e.getId())));
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

    private List<Long> getTermIds(long reviewedAnnotationId) {
        return reviewedAnnotationRepository.findTermIds(reviewedAnnotationId);
    }
}
