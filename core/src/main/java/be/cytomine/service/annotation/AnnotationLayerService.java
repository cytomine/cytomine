package be.cytomine.service.annotation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.dto.annotation.AnnotationLayerResponse;
import be.cytomine.repository.annotation.AnnotationLayerRepository;
import be.cytomine.repository.appengine.TaskRunLayerRepository;
import be.cytomine.repository.appengine.TaskRunRepository;
import be.cytomine.service.image.ImageInstanceService;

@Service
@RequiredArgsConstructor
public class AnnotationLayerService {

    private final AnnotationLayerRepository annotationLayerRepository;

    private final TaskRunLayerRepository taskRunLayerRepository;

    private final TaskRunRepository taskRunRepository;

    private final ImageInstanceService imageInstanceService;

    public String createLayerName(String taskName, String taskVersion, Date created) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdTime = sdf.format(created);
        return taskName + " (" + taskVersion + ") - " + createdTime;
    }

    public AnnotationLayer createAnnotationLayer(String name) {
        return annotationLayerRepository.findByName(name).orElseGet(() -> {
            AnnotationLayer annotationLayer = new AnnotationLayer();
            annotationLayer.setName(name);
            return annotationLayerRepository.saveAndFlush(annotationLayer);
        });
    }

    public Optional<AnnotationLayer> find(Long id) {
        return annotationLayerRepository.findById(id);
    }

    public Set<AnnotationLayerResponse> findByTaskRunLayer(Long imageId) {
        Stream<AnnotationLayer> taskRunLayers =
            taskRunLayerRepository.findAllByImageId(imageId).stream().map(TaskRunLayer::getAnnotationLayer);

        ImageInstance imageInstance = imageInstanceService.get(imageId);
        Stream<AnnotationLayer> lastTaskRunLayers =
            taskRunRepository.findFirstByProjectIdOrderByCreatedDesc(imageInstance.getProject().getId()).stream()
                .flatMap(
                    existingTaskRun -> taskRunLayerRepository.findAllByTaskRunAndImage(existingTaskRun, imageInstance)
                        .stream()).map(TaskRunLayer::getAnnotationLayer);

        return Stream.concat(taskRunLayers, lastTaskRunLayers)
            .map(layer -> new AnnotationLayerResponse(layer.getId(), layer.getName()))
            .collect(Collectors.toSet());
    }
}
