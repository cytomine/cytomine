package be.cytomine.controller.repository;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.ReviewedAnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.ReviewedAnnotationResponse;
import be.cytomine.common.repository.model.reviewedannotation.payload.CreateReviewedAnnotation;
import be.cytomine.common.repository.model.reviewedannotation.payload.UpdateReviewedAnnotation;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewedAnnotationController {

    private final ReviewedAnnotationHttpContract reviewedAnnotationHttpContract;
    private final CurrentUserService currentUserService;

    @GetMapping("reviewedannotation/{id}.json")
    public ReviewedAnnotationResponse get(@PathVariable long id) {
        log.debug("REST request to get reviewed annotation {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return reviewedAnnotationHttpContract.findById(id, userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                format("Unable to find reviewed annotation with id: %s", id)));
    }

    @GetMapping("reviewedannotation/count/term/{termId}.json")
    public long countByTerm(@PathVariable long termId) {
        log.debug("REST request to count reviewed annotations for term {}", termId);
        long userId = currentUserService.getCurrentUser().getId();
        return reviewedAnnotationHttpContract.countByTerm(termId, userId);
    }

    @PostMapping("reviewedannotation.json")
    public Optional<HttpCommandResponse> create(@RequestBody @Valid CreateReviewedAnnotation createReviewedAnnotation) {
        long userId = currentUserService.getCurrentUser().getId();
        log.debug("REST request to create reviewed annotation for user {}", userId);
        return reviewedAnnotationHttpContract.create(userId, createReviewedAnnotation);
    }

    @PutMapping("reviewedannotation/{id}.json")
    public HttpCommandResponse update(@PathVariable long id,
                                      @RequestBody @Valid UpdateReviewedAnnotation update) {
        log.debug("REST request to update reviewed annotation {}", id);
        Optional<HttpCommandResponse> result = reviewedAnnotationHttpContract.update(
            id, currentUserService.getCurrentUser().getId(), update);
        return result.orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
            format("Unable to find reviewed annotation with id: %d", id)));
    }

    @DeleteMapping("reviewedannotation/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("REST request to delete reviewed annotation {}", id);
        Optional<HttpCommandResponse> result = reviewedAnnotationHttpContract.delete(
            id, currentUserService.getCurrentUser().getId());
        return result.orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
            format("Unable to find reviewed annotation with id: %d", id)));
    }
}
