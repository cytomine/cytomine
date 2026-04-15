package be.cytomine.controller.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.dto.annotation.AnnotationResponse;
import be.cytomine.repository.annotation.AnnotationRepository;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnnotationController {

    private final AnnotationRepository annotationRepository;

    @GetMapping("/annotations/{id}")
    public AnnotationResponse getById(@PathVariable long id) {
        log.debug("REST request to get annotation {}", id);
        return annotationRepository.findById(id)
            .map(a -> new AnnotationResponse(a.getId(), a.getAnnotationLayer().getId(), a.getLocation()))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                format("Unable to find annotation with id: %d", id)));
    }
}
