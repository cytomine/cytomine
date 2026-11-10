package be.cytomine.unit.service.annotation;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.dto.annotation.AnnotationLayerResponse;
import be.cytomine.repository.annotation.AnnotationLayerRepository;
import be.cytomine.repository.appengine.TaskRunLayerRepository;
import be.cytomine.repository.appengine.TaskRunRepository;
import be.cytomine.service.annotation.AnnotationLayerService;
import be.cytomine.service.image.ImageInstanceService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnnotationLayerServiceTest {

    private static AnnotationLayer mockAnnotationLayer;
    private static ImageInstance mockImage;
    private static TaskRun mockTaskRun;
    private static TaskRunLayer mockTaskRunLayer;
    private static String name;
    private static Project mockProject;
    @Mock
    private AnnotationLayerRepository annotationLayerRepository;
    @Mock
    private TaskRunRepository taskRunRepository;
    @Mock
    private TaskRunLayerRepository taskRunLayerRepository;
    @Mock
    private ImageInstanceService imageInstanceService;
    @InjectMocks
    private AnnotationLayerService annotationLayerService;

    @BeforeAll
    public static void setUp() {
        name = UUID.randomUUID().toString();
        mockAnnotationLayer = new AnnotationLayer();
        mockAnnotationLayer.setName(name);

        mockProject = new Project();
        mockProject.setId(1L);

        mockImage = new ImageInstance();
        mockImage.setId(1L);
        mockImage.setProject(mockProject);

        mockTaskRun = new TaskRun();
        mockTaskRun.setImage(mockImage);

        mockTaskRunLayer = new TaskRunLayer();
        mockTaskRunLayer.setAnnotationLayer(mockAnnotationLayer);
        mockTaskRunLayer.setTaskRun(mockTaskRun);
        mockTaskRunLayer.setImage(mockImage);
    }

    @Test
    public void createAnnotationLayerShouldReturnAnnotationLayer() {
        when(annotationLayerRepository.saveAndFlush(any(AnnotationLayer.class))).thenReturn(mockAnnotationLayer);

        AnnotationLayer result = annotationLayerService.createAnnotationLayer(name);

        assertNotNull(result);
        assertEquals(mockAnnotationLayer.getId(), result.getId());
        assertEquals(mockAnnotationLayer.getName(), result.getName());

        verify(annotationLayerRepository, times(1)).saveAndFlush(any(AnnotationLayer.class));
    }

    @Test
    public void findShouldReturnAnnotationLayer() {
        when(annotationLayerRepository.findById(mockAnnotationLayer.getId()))
            .thenReturn(Optional.of(mockAnnotationLayer));

        Optional<AnnotationLayer> result = annotationLayerService.find(mockAnnotationLayer.getId());

        assertTrue(result.isPresent());
        assertEquals(mockAnnotationLayer, result.get());

        verify(annotationLayerRepository, times(1)).findById(mockAnnotationLayer.getId());
    }

    @Test
    public void findShouldReturnEmpty() {
        when(annotationLayerRepository.findById(mockAnnotationLayer.getId())).thenReturn(Optional.empty());

        Optional<AnnotationLayer> result = annotationLayerService.find(mockAnnotationLayer.getId());

        assertTrue(result.isEmpty());

        verify(annotationLayerRepository, times(1)).findById(mockAnnotationLayer.getId());
    }

    @Test
    public void findByTaskRunLayerShouldReturnAnnotationLayers() {
        List<TaskRunLayer> mockTaskRunLayers = List.of(mockTaskRunLayer, mockTaskRunLayer);
        when(taskRunLayerRepository.findAllByImageId(mockImage.getId())).thenReturn(mockTaskRunLayers);
        when(imageInstanceService.get(mockImage.getId())).thenReturn(mockImage);
        when(taskRunLayerRepository.findAllByTaskRunAndImage(mockTaskRun, mockImage))
            .thenReturn(Set.of(mockTaskRunLayer));
        when(taskRunRepository.findFirstByProjectIdOrderByCreatedDesc(mockProject.getId())).thenReturn(Optional.of(
            mockTaskRun));

        Set<AnnotationLayerResponse> results = annotationLayerService.findByTaskRunLayer(mockImage.getId());

        assertFalse(results.isEmpty());
        assertEquals(mockTaskRunLayers.size(), results.size() + 1);
        verify(taskRunLayerRepository, times(1)).findAllByImageId(mockImage.getId());
        verify(taskRunLayerRepository, times(1)).findAllByTaskRunAndImage(mockTaskRun, mockImage);
    }

    @Test
    public void findByTaskRunLayerShouldReturnAllLayersOfLastTaskRun() {
        // an app with several geometry outputs creates one annotation layer per output for a single task run
        AnnotationLayer secondAnnotationLayer = new AnnotationLayer();
        secondAnnotationLayer.setId(2L);
        secondAnnotationLayer.setName(name + "-second");

        TaskRunLayer secondLayer = new TaskRunLayer();
        secondLayer.setId(101L);
        secondLayer.setAnnotationLayer(secondAnnotationLayer);
        secondLayer.setTaskRun(mockTaskRun);
        secondLayer.setImage(mockImage);

        when(taskRunLayerRepository.findAllByImageId(mockImage.getId())).thenReturn(List.of());
        when(imageInstanceService.get(mockImage.getId())).thenReturn(mockImage);
        when(taskRunRepository.findFirstByProjectIdOrderByCreatedDesc(mockProject.getId())).thenReturn(Optional.of(
            mockTaskRun));
        when(taskRunLayerRepository.findAllByTaskRunAndImage(mockTaskRun, mockImage))
            .thenReturn(Set.of(mockTaskRunLayer, secondLayer));

        Set<AnnotationLayerResponse> results = annotationLayerService.findByTaskRunLayer(mockImage.getId());

        assertEquals(2, results.size());
        assertTrue(results.contains(new AnnotationLayerResponse(mockAnnotationLayer.getId(), name)));
        assertTrue(results.contains(new AnnotationLayerResponse(2L, name + "-second")));
    }

    @Test
    public void findByTaskRunLayerShouldNotAddLayersFromAnotherImage() {
        ImageInstance otherImage = new ImageInstance();
        otherImage.setId(2L);
        otherImage.setProject(mockProject);

        TaskRun taskRunOnOtherImage = new TaskRun();
        taskRunOnOtherImage.setImage(otherImage);

        AnnotationLayer otherImageLayer = new AnnotationLayer();
        otherImageLayer.setId(3L);
        otherImageLayer.setName(name + "-other-image");

        TaskRunLayer layerOnOtherImage = new TaskRunLayer();
        layerOnOtherImage.setAnnotationLayer(otherImageLayer);
        layerOnOtherImage.setTaskRun(taskRunOnOtherImage);
        layerOnOtherImage.setImage(otherImage);

        when(taskRunLayerRepository.findAllByImageId(mockImage.getId())).thenReturn(List.of(mockTaskRunLayer));
        when(imageInstanceService.get(mockImage.getId())).thenReturn(mockImage);
        when(taskRunRepository.findFirstByProjectIdOrderByCreatedDesc(mockProject.getId()))
            .thenReturn(Optional.of(taskRunOnOtherImage));
        when(taskRunLayerRepository.findAllByTaskRunAndImage(taskRunOnOtherImage, mockImage)).thenReturn(Set.of());

        Set<AnnotationLayerResponse> results = annotationLayerService.findByTaskRunLayer(mockImage.getId());

        assertEquals(1, results.size());
        assertTrue(results.contains(new AnnotationLayerResponse(mockAnnotationLayer.getId(), name)));
        assertFalse(results.contains(new AnnotationLayerResponse(3L, name + "-other-image")));

        verify(taskRunLayerRepository, times(1)).findAllByTaskRunAndImage(taskRunOnOtherImage, mockImage);
        verify(taskRunLayerRepository, never()).findAllByTaskRunAndImage(taskRunOnOtherImage, otherImage);
    }
}
