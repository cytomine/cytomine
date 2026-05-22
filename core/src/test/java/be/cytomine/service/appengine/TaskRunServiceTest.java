package be.cytomine.service.appengine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.CropOffset;
import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.appengine.task.TaskRunValue;
import be.cytomine.dto.appengine.task.output.CollectionOutput;
import be.cytomine.dto.appengine.task.output.GeometryOutput;
import be.cytomine.repository.appengine.TaskRunLayerRepository;
import be.cytomine.repository.appengine.TaskRunRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.annotation.AnnotationLayerService;
import be.cytomine.service.annotation.AnnotationService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.service.utils.GeometryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
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

        Geometry inputGeometry1 = mock(Geometry.class);
        Geometry inputGeometry2 = mock(Geometry.class);

        CollectionOutput.IndexedTaskRunOutput geoItem1 = new CollectionOutput.IndexedTaskRunOutput(
            new GeometryOutput(null, null, null, inputGeometry1),
            0
        );
        CollectionOutput.IndexedTaskRunOutput geoItem2 = new CollectionOutput.IndexedTaskRunOutput(
            new GeometryOutput(null, null, null, inputGeometry2),
            1
        );

        CollectionOutput nestedGeo1 = new CollectionOutput(
            taskRunId,
            "input",
            "ARRAY",
            List.of(geoItem1),
            "GEOMETRY"
        );
        CollectionOutput nestedGeo2 = new CollectionOutput(
            taskRunId,
            "input",
            "ARRAY",
            List.of(geoItem2),
            "GEOMETRY"
        );

        CollectionOutput.IndexedTaskRunOutput arrayItem1 = new CollectionOutput.IndexedTaskRunOutput(
            nestedGeo1,
            0
        );
        CollectionOutput.IndexedTaskRunOutput arrayItem2 = new CollectionOutput.IndexedTaskRunOutput(
            nestedGeo2,
            1
        );

        CollectionOutput collectionOutput = new CollectionOutput(
            taskRunId,
            "input",
            "ARRAY",
            List.of(arrayItem1, arrayItem2),
            "ARRAY"
        );

        TaskRunValue geometryArrayValue = new TaskRunValue(
            taskRunId,
            "input",
            "ARRAY",
            List.of(
                Map.of("value", geoJson1),
                Map.of("value", geoJson2)
            ),
            "GEOMETRY"
        );

        List<TaskRunValue> outputs = List.of(geometryArrayValue);
        String outputsJson = objectMapper.writeValueAsString(outputs);

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
        taskRunLayer.setParameterName("input");
        taskRunLayer.setOffsets(List.of(new CropOffset(0, 0)));
        AnnotationLayer annotationLayer = new AnnotationLayer();

        doNothing().when(securityACLService).checkUser(currentUser);
        doNothing().when(securityACLService).check(project, READ);
        doNothing().when(securityACLService).checkIsNotReadOnly(project);
        when(annotationLayerService.createLayerName(any(), any(), any())).thenReturn("layer-name");
        when(annotationLayerService.createAnnotationLayer("layer-name")).thenReturn(annotationLayer);
        when(appEngineService.get("task-runs/" + taskRunId + "/outputs")).thenReturn(outputsJson);
        when(appEngineService.get("task-runs/" + taskRunId)).thenReturn(taskRunJson);
        when(appEngineService.getTaskRunOutputs(taskRunId)).thenReturn(List.of(collectionOutput));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(geometryService.addOffset(
            any(Geometry.class),
            anyInt(),
            anyInt()
        )).thenReturn(Optional.of(mock(Geometry.class)));
        when(projectService.get(projectId)).thenReturn(project);
        when(taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)).thenReturn(Optional.of(taskRun));
        when(taskRunLayerRepository.findAllByTaskRunAndImage(any(), any())).thenReturn(Set.of(taskRunLayer));

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

        Geometry inputGeometry1 = mock(Geometry.class);
        Geometry inputGeometry2 = mock(Geometry.class);

        CollectionOutput nestedGeo1 = new CollectionOutput(
            taskRunId, "inner", "ARRAY",
            List.of(new CollectionOutput.IndexedTaskRunOutput(
                new GeometryOutput(taskRunId, "inner", "GEOMETRY", inputGeometry1),
                0

            )),
            "GEOMETRY"
        );
        CollectionOutput nestedGeo2 = new CollectionOutput(
            taskRunId, "inner", "ARRAY",
            List.of(new CollectionOutput.IndexedTaskRunOutput(
                new GeometryOutput(taskRunId, "inner", "GEOMETRY", inputGeometry2),
                1
            )),
            "GEOMETRY"
        );

        CollectionOutput innerArray = new CollectionOutput(
            taskRunId, "inner", "ARRAY",
            List.of(
                new CollectionOutput.IndexedTaskRunOutput(nestedGeo1, 0),
                new CollectionOutput.IndexedTaskRunOutput(nestedGeo2, 1)
            ),
            "ARRAY"
        );

        CollectionOutput outerArray = new CollectionOutput(
            taskRunId, "outer", "ARRAY",
            List.of(new CollectionOutput.IndexedTaskRunOutput(innerArray, 0)),
            "ARRAY"
        );

        TaskRunValue outerArrayValue = new TaskRunValue(
            taskRunId,
            "outer",
            "ARRAY",
            List.of(new TaskRunValue(
                taskRunId, "inner", "ARRAY",
                List.of(
                    Map.of("value", geoJson1),
                    Map.of("value", geoJson2)
                ),
                "GEOMETRY"
            )),
            "ARRAY"
        );

        List<TaskRunValue> outputs = List.of(outerArrayValue);
        String outputsJson = objectMapper.writeValueAsString(outputs);

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
        taskRunLayer.setParameterName("outer");
        taskRunLayer.getOffsets().add(new CropOffset(0, 0));

        AnnotationLayer annotationLayer = new AnnotationLayer();

        doNothing().when(securityACLService).checkUser(currentUser);
        doNothing().when(securityACLService).check(project, READ);
        doNothing().when(securityACLService).checkIsNotReadOnly(project);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(annotationLayerService.createLayerName(any(), any(), any())).thenReturn("layer-name");
        when(annotationLayerService.createAnnotationLayer("layer-name")).thenReturn(annotationLayer);
        when(appEngineService.get("task-runs/" + taskRunId + "/outputs")).thenReturn(outputsJson);
        when(appEngineService.get("task-runs/" + taskRunId)).thenReturn(taskRunJson);
        when(appEngineService.getTaskRunOutputs(taskRunId)).thenReturn(List.of(outerArray));
        when(projectService.get(projectId)).thenReturn(project);
        when(taskRunLayerRepository.findAllByTaskRunAndImage(any(), any())).thenReturn(Set.of(taskRunLayer));
        when(taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)).thenReturn(Optional.of(taskRun));

        Geometry outputGeometry1 = mock(Geometry.class);
        Geometry outputGeometry2 = mock(Geometry.class);
        when(geometryService.addOffset(any(Geometry.class), eq(0), eq(0)))
            .thenReturn(Optional.of(outputGeometry1))
            .thenReturn(Optional.of(outputGeometry2));

        String result = taskRunService.getOutputs(projectId, taskRunId);

        assertEquals(outputsJson, result);
        verify(annotationService, times(2)).createAnnotation(eq(annotationLayer), any(String.class));
        verify(asyncService, times(1)).launchImageAdditionJob(
            ArgumentMatchers.any(), eq(projectId), eq(currentUser)
        );
    }
}
