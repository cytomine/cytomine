package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.ReviewedAnnotationMapper;
import org.cytomine.repository.mapper.UserAnnotationMapper;
import org.cytomine.repository.persistence.ReviewedAnnotationRepository;
import org.cytomine.repository.persistence.UserAnnotationRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.ReviewedAnnotationCommandService;
import org.cytomine.repository.service.UserAnnotationCommandService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.AnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.reviewedannotation.payload.UpdateReviewedAnnotation;
import be.cytomine.common.repository.model.userannotation.payload.UpdateUserAnnotation;

import static be.cytomine.common.repository.http.AnnotationHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class AnnotationController implements AnnotationHttpContract {

    private final UserAnnotationRepository userAnnotationRepository;
    private final ReviewedAnnotationRepository reviewedAnnotationRepository;
    private final UserAnnotationCommandService userAnnotationCommandService;
    private final ReviewedAnnotationCommandService reviewedAnnotationCommandService;
    private final UserAnnotationMapper userAnnotationMapper;
    private final ReviewedAnnotationMapper reviewedAnnotationMapper;
    private final ACLService aclService;

    @Override
    @GetMapping("/{id}")
    public Optional<ApplyCommandResponse> findById(long id, long userId) {
        Optional<ApplyCommandResponse> result = userAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(e -> (ApplyCommandResponse) userAnnotationMapper.mapToResponse(e));
        if (result.isPresent()) {
            return result;
        }
        return reviewedAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(e -> (ApplyCommandResponse) reviewedAnnotationMapper.mapToResponse(e,
                reviewedAnnotationRepository.findTermIds(e.getId())));
    }

    @Override
    @PutMapping("/{id}")
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateUserAnnotation update) {
        if (userAnnotationRepository.existsById(id)) {
            return userAnnotationCommandService.updateUserAnnotation(id, userId, update, LocalDateTime.now());
        }
        return reviewedAnnotationCommandService.updateReviewedAnnotation(id, userId,
            new UpdateReviewedAnnotation(update.wktLocation(), update.geometryCompression()), LocalDateTime.now());
    }

    @Override
    @DeleteMapping("/{id}")
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        if (userAnnotationRepository.existsById(id)) {
            return userAnnotationCommandService.deleteUserAnnotation(id, userId, LocalDateTime.now());
        }
        return reviewedAnnotationCommandService.deleteReviewedAnnotation(id, userId, LocalDateTime.now());
    }
}
