package be.cytomine.service.annotation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.appengine.TaskRunRepository;
import be.cytomine.repository.image.ImageInstanceRepository;
import be.cytomine.service.appengine.TaskRunService;
import be.cytomine.service.image.ImageInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.domain.annotation.Annotation;
import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.dto.appengine.task.TaskRunLayerValue;
import be.cytomine.repository.annotation.AnnotationLayerRepository;
import be.cytomine.repository.annotation.AnnotationRepository;
import be.cytomine.repository.appengine.TaskRunLayerRepository;
import be.cytomine.service.appengine.TaskRunLayerService;

@Service
@RequiredArgsConstructor
public class AnnotationLayerService {

    private final AnnotationRepository annotationRepository;

    private final AnnotationLayerRepository annotationLayerRepository;

    private final TaskRunLayerRepository taskRunLayerRepository;

    private final TaskRunLayerService taskRunLayerService;

    private final TaskRunRepository taskRunRepository;

    private final ImageInstanceService imageInstanceService;

    public String createLayerName(String taskName, String taskVersion, Date created) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdTime = sdf.format(created);
        return taskName + " (" + taskVersion + ") - " + createdTime;
    }

    public AnnotationLayer createAnnotationLayer(String name) {
        return annotationLayerRepository.findByName(name).orElseGet(()->{
            AnnotationLayer annotationLayer = new AnnotationLayer();
            annotationLayer.setName(name);
            return annotationLayerRepository.saveAndFlush(annotationLayer);
        });
    }

    public Optional<AnnotationLayer> find(Long id) {
        return annotationLayerRepository.findById(id);
    }

    public List<AnnotationLayer> findByTaskRunLayer(Long imageId) {
        List<TaskRunLayer> taskRunLayers = taskRunLayerRepository.findAllByImageId(imageId);

        List<AnnotationLayer> annotationLayerList = new ArrayList<>(taskRunLayers
            .stream()
            .map(TaskRunLayer::getAnnotationLayer)
            .toList());

        ImageInstance imageInstance = imageInstanceService.get(imageId);

        Optional<TaskRun> lastTaskRun = taskRunRepository
            .findFirstByProjectIdOrderByCreatedDesc(imageInstance.getProject().getId());

        if (lastTaskRun.isPresent()) {
            Optional<TaskRunLayer> lastExecutedRunLayer = taskRunLayerRepository.findByTaskRun(lastTaskRun.get());

            List<AnnotationLayer> lastTaskRunAnnotationLayers = lastExecutedRunLayer
                .stream()
                .map(TaskRunLayer::getAnnotationLayer)
                .toList();

            annotationLayerList.addAll(lastTaskRunAnnotationLayers);
        }

        return annotationLayerList.stream().distinct().toList();
    }

    public TaskRunLayerValue findTaskRunLayer(Long id) {
        Optional<TaskRunLayer> optional = taskRunLayerRepository.findByAnnotationLayerId(id);
        if (optional.isEmpty()) {
            return null;
        }

        return taskRunLayerService.convertToDTO(optional.get());
    }

    public List<Annotation> findAnnotationsByLayer(AnnotationLayer layer) {
        return annotationRepository.findAllByAnnotationLayer(layer);
    }
}
