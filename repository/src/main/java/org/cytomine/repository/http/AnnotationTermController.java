package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.AnnotationTermMapper;
import org.cytomine.repository.persistence.AnnotationTermRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.AnnotationTermCommandService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.AnnotationTermHttpContract;
import be.cytomine.common.repository.model.annotationterm.payload.CreateAnnotationTerm;
import be.cytomine.common.repository.model.command.payload.response.AnnotationTermResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;

import static be.cytomine.common.repository.http.AnnotationTermHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class AnnotationTermController implements AnnotationTermHttpContract {
    private final AnnotationTermRepository annotationTermRepository;
    private final AnnotationTermCommandService annotationTermCommandService;
    private final AnnotationTermMapper annotationTermMapper;
    private final ACLService aclService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @GetMapping("/{id}")
    public Optional<AnnotationTermResponse> findById(long id, long userId) {
        return annotationTermRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, getProjectId(e.getUserAnnotationId())))
            .map(annotationTermMapper::mapToResponse);
    }

    @Override
    @GetMapping("/user_annotation/{annotationId}")
    public List<AnnotationTermResponse> findByUserAnnotation(long annotationId, long userId) {
        return annotationTermRepository.findAllByUserAnnotationId(annotationId).stream()
            .map(annotationTermMapper::mapToResponse)
            .toList();
    }

    @Override
    @GetMapping("/user_annotation/{annotationId}/user/{termUserId}")
    public List<AnnotationTermResponse> findByUserAnnotationAndUser(long annotationId,
                                                                    long termUserId,
                                                                    long userId) {
        return annotationTermRepository.findAllByUserAnnotationIdAndUserId(annotationId, termUserId).stream()
            .map(annotationTermMapper::mapToResponse)
            .toList();
    }

    @Override
    @GetMapping("/project/{projectId}")
    public List<AnnotationTermResponse> findByProject(long projectId, long userId) {
        if (!aclService.canReadProject(userId, projectId)) {
            return List.of();
        }
        return annotationTermRepository.findAllByProjectId(projectId).stream()
            .map(annotationTermMapper::mapToResponse)
            .toList();
    }

    @Override
    @GetMapping("/term/{termId}/count")
    public long countByTerm(long termId, long userId) {
        return annotationTermRepository.countByTermId(termId);
    }

    @Override
    @GetMapping("/find")
    public Optional<AnnotationTermResponse> findByAnnotationAndTermAndUser(long annotationId,
                                                                           long termId,
                                                                           long termUserId,
                                                                           long userId) {
        return annotationTermRepository.findByUserAnnotationIdAndTermIdAndUserId(annotationId, termId, termUserId)
            .map(annotationTermMapper::mapToResponse);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateAnnotationTerm createAnnotationTerm) {
        return annotationTermCommandService.createAnnotationTerm(userId, createAnnotationTerm,
            LocalDateTime.now());
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return annotationTermCommandService.deleteAnnotationTerm(id, userId, LocalDateTime.now());
    }

    private long getProjectId(long userAnnotationId) {
        Long projectId = jdbcTemplate.queryForObject(
            "SELECT project_id FROM user_annotation WHERE id = ?", Long.class, userAnnotationId);
        return projectId != null ? projectId : 0L;
    }
}
