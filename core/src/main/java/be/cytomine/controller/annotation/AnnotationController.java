package be.cytomine.controller.annotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.AnnotationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.service.CurrentUserService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AnnotationController {

    private final AnnotationHttpContract annotationHttpContract;
    private final CurrentUserService currentUserService;

    @GetMapping("/annotations/{id}")
    public ApplyCommandResponse getById(@PathVariable Long id) {
        log.info("Retrieve annotation {}", id);
        long userId = currentUserService.getCurrentUser().getId();
        return annotationHttpContract.findById(id, userId)
            .orElseThrow(
                () -> new ResponseStatusException(NOT_FOUND, format("Unable to find annotation with id: %d", id)));
    }
}
