package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.UserAnnotationMapper;
import org.cytomine.repository.persistence.UserAnnotationRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.UserAnnotationCommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.UserAnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserAnnotationResponse;
import be.cytomine.common.repository.model.userannotation.payload.CreateUserAnnotation;
import be.cytomine.common.repository.model.userannotation.payload.UpdateUserAnnotation;

import static be.cytomine.common.repository.http.UserAnnotationHttpContract.ROOT_PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class UserAnnotationController implements UserAnnotationHttpContract {
    private final UserAnnotationRepository userAnnotationRepository;
    private final UserAnnotationCommandService userAnnotationCommandService;
    private final UserAnnotationMapper userAnnotationMapper;
    private final ACLService aclService;

    @Override
    @GetMapping("/{id}")
    public Optional<UserAnnotationResponse> findById(long id, long userId) {
        return userAnnotationRepository.findById(id)
            .filter(e -> aclService.canReadProject(userId, e.getProjectId()))
            .map(userAnnotationMapper::mapToResponse);
    }

    @Override
    @GetMapping("/user/{userAnnotationUserId}/image/{imageId}")
    public List<UserAnnotationResponse> findAllByUserAndImage(long userAnnotationUserId,
                                                              long imageId,
                                                              long userId) {
        return userAnnotationRepository.findAllByUserIdAndImageId(userAnnotationUserId, imageId).stream()
            .map(userAnnotationMapper::mapToResponse)
            .toList();
    }

    @Override
    @GetMapping("/count/project/{projectId}")
    public long countByProject(long projectId, long userId) {
        if (!aclService.canReadProject(userId, projectId)) {
            return 0L;
        }
        return userAnnotationRepository.countByProjectId(projectId);
    }

    @Override
    @GetMapping("/count/user/{userAnnotationUserId}")
    public long countByUser(long userAnnotationUserId, long userId) {
        return userAnnotationRepository.countByUserId(userAnnotationUserId);
    }

    @Override
    @GetMapping("/count/user/{userAnnotationUserId}/project/{projectId}")
    public long countByUserAndProject(long userAnnotationUserId, long projectId, long userId) {
        if (!aclService.canReadProject(userId, projectId)) {
            return 0L;
        }
        return userAnnotationRepository.countByUserIdAndProjectId(userAnnotationUserId, projectId);
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateUserAnnotation createUserAnnotation) {
        return userAnnotationCommandService.createUserAnnotation(userId, createUserAnnotation,
            LocalDateTime.now());
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateUserAnnotation updateUserAnnotation) {
        return userAnnotationCommandService.updateUserAnnotation(id, userId, updateUserAnnotation,
            LocalDateTime.now());
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return userAnnotationCommandService.deleteUserAnnotation(id, userId, LocalDateTime.now());
    }
}
