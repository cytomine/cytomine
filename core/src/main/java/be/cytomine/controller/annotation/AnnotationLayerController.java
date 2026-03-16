package be.cytomine.controller.annotation;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.dto.annotation.AnnotationLayerResponse;
import be.cytomine.dto.annotation.AnnotationResponse;
import be.cytomine.dto.appengine.task.TaskRunLayerValue;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.annotation.AnnotationRepository;
import be.cytomine.repository.appengine.TaskRunLayerRepository;
import be.cytomine.service.annotation.AnnotationLayerService;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AnnotationLayerController {

    private final AnnotationLayerService annotationLayerService;

    private final AnnotationRepository annotationRepository;

    private final TaskRunLayerRepository taskRunLayerRepository;

    @GetMapping("/image-instances/{id}/annotation-layers")
    public Set<AnnotationLayerResponse> getAnnotationLayersByImage(@PathVariable Long id) {
        log.info("GET /image-instances/{}/annotation-layers", id);
        return annotationLayerService.findByTaskRunLayer(id);
    }

    @GetMapping("/annotation-layers/{id}/annotations")
    public Set<AnnotationResponse> getAnnotationsByLayer(@PathVariable Long id) {
        log.info("GET /annotation-layers/{}/annotations", id);

        AnnotationLayer layer = annotationLayerService
                .find(id)
                .orElseThrow(() -> new ObjectNotFoundException("AnnotationLayer " + id + " not found"));

        return annotationRepository.findAllByAnnotationLayer(layer)
                .stream()
                .map(annotation -> new AnnotationResponse(
                        annotation.getId(),
                        annotation.getAnnotationLayer().getId(),
                        annotation.getLocation()
                ))
                .collect(Collectors.toSet());
    }

    @GetMapping("/annotation-layers/{id}/task-run-layer")
    public TaskRunLayerValue findTaskRunLayer(@PathVariable Long id) {
        log.info("GET /annotation-layers/{}/task-run-layer", id);
        return taskRunLayerRepository.findByAnnotationLayerId(id)
                .map(trl -> new TaskRunLayerValue(
                        trl.getAnnotationLayer().getId(),
                        trl.getTaskRun().getId(),
                        trl.getImage().getId()
                ))
                .orElseThrow(() -> new ObjectNotFoundException("TaskRunLayer", id));
    }
}
