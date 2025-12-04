package be.cytomine.controller.annotation;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.domain.annotation.Annotation;
import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.dto.annotation.AnnotationLayerResponse;
import be.cytomine.dto.appengine.task.TaskRunLayerValue;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.service.annotation.AnnotationLayerService;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AnnotationLayerController {

    private final AnnotationLayerService annotationLayerService;

    @GetMapping("/image-instances/{id}/annotation-layers")
    public ResponseEntity<List<AnnotationLayerResponse>> getAnnotationLayersByImage(@PathVariable Long id) {
        log.info("GET /image-instances/{}/annotation-layers", id);
        List<AnnotationLayer> layers = annotationLayerService.findByTaskRunLayer(id);
        List<AnnotationLayerResponse> responses = layers.stream()
                .map(layer -> new AnnotationLayerResponse(layer.getId(), layer.getName(), id))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/annotation-layers/{id}/annotations")
    public ResponseEntity<List<Annotation>> getAnnotationsByLayer(@PathVariable Long id) {
        log.info("Retrieve all annotations for annotation layer {}", id);

        AnnotationLayer layer = annotationLayerService
                .find(id)
                .orElseThrow(() -> new ObjectNotFoundException("AnnotationLayer " + id + " not found"));

        return ResponseEntity.ok(annotationLayerService.findAnnotationsByLayer(layer));
    }

    @GetMapping("/annotation-layers/{id}/task-run-layer")
    public ResponseEntity<TaskRunLayerValue> findTaskRunLayer(@PathVariable Long id) {
        log.info("Restrieve all the task run layers for annotation layer {}", id);
        return ResponseEntity.ok(annotationLayerService.findTaskRunLayer(id));
    }
}
