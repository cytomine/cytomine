package be.cytomine.controller.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.UserAnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UserAnnotationResponse;
import be.cytomine.common.repository.model.userannotation.payload.CreateUserAnnotation;
import be.cytomine.common.repository.model.userannotation.payload.UpdateUserAnnotation;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserAnnotationController {

    private final UserAnnotationHttpContract userAnnotationHttpContract;
    private final CurrentUserService currentUserService;

    @GetMapping("userannotation/{id}.json")
    public UserAnnotationResponse get(@PathVariable long id) {
        log.debug("REST request to get user annotation {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return userAnnotationHttpContract.findById(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                format("Unable to find user annotation with id: %s", id)));
    }

    @GetMapping("userannotation/user/{userAnnotationUserId}/image/{imageId}.json")
    public List<UserAnnotationResponse> listByUserAndImage(
        @PathVariable long userAnnotationUserId,
        @PathVariable long imageId) {
        log.debug("REST request to list user annotations for user {} and image {}", userAnnotationUserId, imageId);
        return userAnnotationHttpContract.findAllByUserAndImage(
            userAnnotationUserId, imageId, currentUserService.getCurrentUser().getId());
    }

    @GetMapping("project/{projectId}/userannotation/count.json")
    public Map<String, Long> countByProject(@PathVariable long projectId,
                                            @RequestParam(required = false) Long startDate,
                                            @RequestParam(required = false) Long endDate) {
        log.debug("REST request to count user annotations for project {}", projectId);
        long userId = currentUserService.getCurrentUser().getId();
        return Map.of("total", userAnnotationHttpContract.countByProject(projectId, userId, startDate, endDate));
    }

    @GetMapping("user/{userAnnotationUserId}/userannotation/count.json")
    public Map<String, Long> countByUser(@PathVariable long userAnnotationUserId,
                                         @RequestParam(name = "project", required = false) Long projectId) {
        long userId = currentUserService.getCurrentUser().getId();
        if (projectId != null) {
            log.debug("REST request to count user annotations for user {} and project {}", userAnnotationUserId,
                projectId);
            return Map.of("total", userAnnotationHttpContract.countByUserAndProject(userAnnotationUserId, projectId, userId));
        }
        log.debug("REST request to count user annotations for user {}", userAnnotationUserId);
        return Map.of("total", userAnnotationHttpContract.countByUser(userAnnotationUserId, userId));
    }

    @PostMapping("userannotation.json")
    public Optional<HttpCommandResponse> create(@RequestBody @Valid CreateUserAnnotation createUserAnnotation) {
        long userId = currentUserService.getCurrentUser().getId();
        log.debug("REST request to create user annotation for user {}", userId);
        return userAnnotationHttpContract.create(userId, createUserAnnotation);
    }

    @PutMapping("userannotation/{id}.json")
    public HttpCommandResponse update(@PathVariable long id,
                                      @RequestBody @Valid UpdateUserAnnotation updateUserAnnotation) {
        log.debug("REST request to update user annotation {}", id);
        Optional<HttpCommandResponse> result = userAnnotationHttpContract.update(
            id, currentUserService.getCurrentUser().getId(), updateUserAnnotation);
        return result.orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
            format("Unable to find user annotation with id: %d", id)));
    }

    @DeleteMapping("userannotation/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("REST request to delete user annotation {}", id);
        Optional<HttpCommandResponse> result = userAnnotationHttpContract.delete(
            id, currentUserService.getCurrentUser().getId());
        return result.orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
            format("Unable to find user annotation with id: %d", id)));
    }
}
