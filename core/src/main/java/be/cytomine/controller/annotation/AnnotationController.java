package be.cytomine.controller.annotation;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.ontology.RestAnnotationDomainController;
import be.cytomine.domain.annotation.Annotation;
import be.cytomine.dto.annotation.AnnotationRequest;
import be.cytomine.dto.annotation.AnnotationResponse;
import be.cytomine.service.annotation.AnnotationService;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/annotations")
@RestController
public class AnnotationController {

    private final AnnotationService annotationService;

    private final RestAnnotationDomainController annotationDomainController;

    @PostMapping
    public ResponseEntity<AnnotationResponse> add(@RequestBody AnnotationRequest annotation) {
        log.info("POST /annotations");

        Annotation saved = annotationService.add(annotation);

        AnnotationResponse response = new AnnotationResponse(
            saved.getId(),
            saved.getAnnotationLayer().getId(),
            saved.getLocation().toString()
        );

        return ResponseEntity
            .created(URI.create("/annotations/" + saved.getId()))
            .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable Long id) throws IOException {
        log.info("GET /annotations/{}", id);

        Optional<Annotation> annotation = annotationService.find(id);
        if (annotation.isPresent()) {
            return ResponseEntity.ok(annotation.get().toJSON());
        }

        // Retro-compatible method to get the annotation.
        return annotationDomainController.show(id);
    }
}
