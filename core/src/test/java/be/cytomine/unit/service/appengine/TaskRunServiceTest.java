package be.cytomine.unit.service.appengine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import be.cytomine.domain.appengine.CropOffset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.appengine.task.TaskRunValue;
import be.cytomine.repository.appengine.TaskRunLayerRepository;
import be.cytomine.repository.appengine.TaskRunRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.annotation.AnnotationLayerService;
import be.cytomine.service.annotation.AnnotationService;
import be.cytomine.service.appengine.AppEngineService;
import be.cytomine.service.appengine.AsyncService;
import be.cytomine.service.appengine.TaskRunService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.service.utils.GeometryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.acls.domain.BasePermission.READ;

@ExtendWith(MockitoExtension.class)
public class TaskRunServiceTest {

    @Mock
    private AnnotationService annotationService;

    @Mock
    private AnnotationLayerService annotationLayerService;

    @Mock
    private AppEngineService appEngineService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GeometryService geometryService;

    @Mock
    private ProjectService projectService;

    @Mock
    private SecurityACLService securityACLService;

    @Mock
    private TaskRunRepository taskRunRepository;

    @Mock
    private TaskRunLayerRepository taskRunLayerRepository;

    @Mock
    private AsyncService asyncService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TaskRunService taskRunService;

    @DisplayName("Successfully create annotations from geometry array output")
    @Test
    public void shouldCreateAnnotationsFromGeometryArrayOutput() throws JsonProcessingException {
        Long projectId = 1L;
        UUID taskRunId = UUID.randomUUID();

        String geoJson1 = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}";
        String geoJson2 = "{\"type\":\"Point\",\"coordinates\":[3.0,4.0]}";

        TaskRunValue geometryArrayValue = new TaskRunValue();
        geometryArrayValue.setType("ARRAY");
        geometryArrayValue.setSubType("GEOMETRY");
        geometryArrayValue.setValue(List.of(
                Map.of("value", geoJson1),
                Map.of("value", geoJson2)
        ));

        List<TaskRunValue> outputs = List.of(geometryArrayValue);
        String outputsJson = new ObjectMapper().writeValueAsString(outputs);

        String taskRunJson = """
                    {
                        "task": {
                            "name": "test-task",
                            "version": "1.0"
                        }
                    }
                """;

        TaskRun taskRun = new TaskRun();
        User currentUser = new User();
        Project project = new Project();

        TaskRunLayer taskRunLayer = new TaskRunLayer();
        CropOffset offset = new CropOffset(0, 0, taskRunLayer, 0);
        taskRunLayer.getOffsets().add(offset);

        AnnotationLayer annotationLayer = new AnnotationLayer();

        when(taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)).thenReturn(Optional.of(taskRun));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(projectService.get(projectId)).thenReturn(project);
        doNothing().when(securityACLService).checkUser(currentUser);
        doNothing().when(securityACLService).check(project, READ);
        doNothing().when(securityACLService).checkIsNotReadOnly(project);

        when(appEngineService.get("task-runs/" + taskRunId + "/outputs")).thenReturn(outputsJson);
        when(appEngineService.get("task-runs/" + taskRunId)).thenReturn(taskRunJson);
        when(annotationLayerService.createLayerName(any(), any(), any())).thenReturn("layer-name");
        when(annotationLayerService.createAnnotationLayer("layer-name")).thenReturn(annotationLayer);
        when(taskRunLayerRepository.findAllByTaskRunAndImage(any(), any())).thenReturn(Set.of(taskRunLayer));
        when(geometryService.isGeometry(geoJson1)).thenReturn(true);
        when(geometryService.isGeometry(geoJson2)).thenReturn(true);
        when(geometryService.GeoJSONToWKT(geoJson1)).thenReturn("POINT (1 2)");
        when(geometryService.GeoJSONToWKT(geoJson2)).thenReturn("POINT (3 4)");

        String result = taskRunService.getOutputs(projectId, taskRunId);

        assertEquals(outputsJson, result);
        verify(annotationService, times(2)).createAnnotation(eq(annotationLayer), any(String.class));
        verify(asyncService, times(1)).launchImageAdditionJob(ArgumentMatchers.any(), eq(projectId), eq(currentUser));
    }

    @DisplayName("Successfully create annotations from nested geometry array output")
    @Test
    public void shouldCreateAnnotationsFromNestedGeometryArrayOutput() throws JsonProcessingException {
        Long projectId = 1L;
        UUID taskRunId = UUID.randomUUID();

        String geoJson1 = "{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}";
        String geoJson2 = "{\"type\":\"Point\",\"coordinates\":[3.0,4.0]}";

        TaskRunValue innerGeometryValue = new TaskRunValue();
        innerGeometryValue.setType("ARRAY");
        innerGeometryValue.setSubType("GEOMETRY");
        innerGeometryValue.setValue(List.of(
                Map.of("value", geoJson1),
                Map.of("value", geoJson2)
        ));

        TaskRunValue outerArrayValue = new TaskRunValue();
        outerArrayValue.setType("ARRAY");
        outerArrayValue.setSubType("ARRAY");
        outerArrayValue.setValue(List.of(innerGeometryValue));

        List<TaskRunValue> outputs = List.of(outerArrayValue);
        String outputsJson = new ObjectMapper().writeValueAsString(outputs);

        String taskRunJson = """
                    {
                        "task": {
                            "name": "my-task",
                            "version": "1.0"
                        }
                    }
                """;

        TaskRun taskRun = new TaskRun();
        User currentUser = new User();
        Project project = new Project();

        TaskRunLayer taskRunLayer = new TaskRunLayer();
        CropOffset offset = new CropOffset(0, 0, taskRunLayer, 0);
        taskRunLayer.getOffsets().add(offset);
        AnnotationLayer annotationLayer = new AnnotationLayer();

        when(taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)).thenReturn(Optional.of(taskRun));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(projectService.get(projectId)).thenReturn(project);
        doNothing().when(securityACLService).checkUser(currentUser);
        doNothing().when(securityACLService).check(project, READ);
        doNothing().when(securityACLService).checkIsNotReadOnly(project);

        when(appEngineService.get("task-runs/" + taskRunId + "/outputs")).thenReturn(outputsJson);
        when(appEngineService.get("task-runs/" + taskRunId)).thenReturn(taskRunJson);
        when(annotationLayerService.createLayerName(any(), any(), any())).thenReturn("layer-name");
        when(annotationLayerService.createAnnotationLayer("layer-name")).thenReturn(annotationLayer);
        when(taskRunLayerRepository.findAllByTaskRunAndImage(any(), any())).thenReturn(Set.of(taskRunLayer));
        when(geometryService.isGeometry(geoJson1)).thenReturn(true);
        when(geometryService.isGeometry(geoJson2)).thenReturn(true);
        when(geometryService.GeoJSONToWKT(geoJson1)).thenReturn("POINT (1 2)");
        when(geometryService.GeoJSONToWKT(geoJson2)).thenReturn("POINT (3 4)");

        String result = taskRunService.getOutputs(projectId, taskRunId);

        assertEquals(outputsJson, result);
        verify(annotationService, times(2)).createAnnotation(eq(annotationLayer), any(String.class));
        verify(asyncService, times(1)).launchImageAdditionJob(
                ArgumentMatchers.any(),
                eq(projectId),
                eq(currentUser)
        );
    }
}
