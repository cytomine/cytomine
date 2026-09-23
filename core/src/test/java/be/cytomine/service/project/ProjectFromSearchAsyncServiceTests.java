package be.cytomine.service.project;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.cytomine.config.security.IncomingAuthorizationContext;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.SearchException;
import be.cytomine.repository.image.ImageInstanceRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.MeiliSearchService;
import be.cytomine.service.image.ImageInstanceService;
import be.cytomine.service.utils.TaskService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectFromSearchAsyncServiceTests {

    @Mock
    private MeiliSearchService meiliSearchService;

    @Mock
    private TaskService taskService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ImageInstanceRepository imageInstanceRepository;

    @Mock
    private ImageInstanceService imageInstanceService;

    private ProjectFromSearchAsyncService asyncService() {
        return new ProjectFromSearchAsyncService(
            meiliSearchService, taskService, projectRepository, imageInstanceRepository, imageInstanceService
        );
    }

    private Task task(long id) {
        return mock(Task.class);
    }

    private Project project(long id) {
        Project project = mock(Project.class);
        lenient().when(project.getId()).thenReturn(id);
        return project;
    }

    private ImageInstance imageInstanceWithBaseImage(long abstractImageId) {
        AbstractImage abstractImage = mock(AbstractImage.class);
        when(abstractImage.getId()).thenReturn(abstractImageId);
        ImageInstance imageInstance = mock(ImageInstance.class);
        when(imageInstance.getBaseImage()).thenReturn(abstractImage);
        return imageInstance;
    }

    private JsonObject createAddCommand(long abstractImageId, long projectId) {
        JsonObject json = new JsonObject();
        json.put("baseImage", abstractImageId);
        json.put("project", projectId);
        return json;
    }

    @Test
    void shouldCreateInstancesForMatchingImagesAndTagOnce() {
        Task task = task(1L);
        Project project = project(7L);
        when(project.getName()).thenReturn("MyProject");
        when(taskService.get(1L)).thenReturn(task);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));

        when(meiliSearchService.searchImageIds("query", List.of("tag=value"))).thenReturn(Set.of(10L, 11L));
        when(imageInstanceRepository.findAllByBaseImageIdInAndProject(any(), eq(project))).thenReturn(List.of());
        when(imageInstanceService.add(any(JsonObject.class))).thenAnswer(invocation -> {
            CommandResponse response = new CommandResponse();
            JsonObject json = invocation.getArgument(0);
            response.setObject(imageInstanceWithBaseImage(((Number) json.get("baseImage")).longValue()));
            response.setStatus(200);
            return response;
        });

        asyncService().run(1L, 7L, "query", List.of("tag=value"), null);

        verify(taskService).updateTask(task, 5, "Searching images");
        verify(imageInstanceService).add(createAddCommand(10L, 7L));
        verify(imageInstanceService).add(createAddCommand(11L, 7L));
        verify(meiliSearchService).addProjectToImages(List.of(10L, 11L), "MyProject");
        verify(taskService).updateTask(eq(task), eq(95), org.mockito.ArgumentMatchers.contains("skipped 0"));
        verify(taskService).finishTask(task);

        InOrder tagModeOrder = inOrder(imageInstanceService);
        tagModeOrder.verify(imageInstanceService).setTagMode(ImageInstanceService.TagMode.DEFER);
        tagModeOrder.verify(imageInstanceService).setTagMode(ImageInstanceService.TagMode.NORMAL);
    }

    @Test
    void shouldSkipAbstractImagesAlreadyInProject() {
        Task task = task(1L);
        Project project = project(7L);
        when(project.getName()).thenReturn("MyProject");
        when(taskService.get(1L)).thenReturn(task);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));

        when(meiliSearchService.searchImageIds("", List.of())).thenReturn(Set.of(10L, 11L));
        List<ImageInstance> existing = List.of(imageInstanceWithBaseImage(10L));
        when(imageInstanceRepository.findAllByBaseImageIdInAndProject(any(), eq(project))).thenReturn(existing);
        when(imageInstanceService.add(any(JsonObject.class))).thenAnswer(invocation -> {
            CommandResponse response = new CommandResponse();
            JsonObject json = invocation.getArgument(0);
            response.setObject(imageInstanceWithBaseImage(((Number) json.get("baseImage")).longValue()));
            response.setStatus(200);
            return response;
        });

        asyncService().run(1L, 7L, "", List.of(), null);

        verify(imageInstanceService).add(createAddCommand(11L, 7L));
        verify(imageInstanceService, never()).add(createAddCommand(10L, 7L));
        verify(meiliSearchService).addProjectToImages(List.of(11L), "MyProject");
        verify(taskService).updateTask(eq(task), eq(95), org.mockito.ArgumentMatchers.contains("skipped 1"));
    }

    @Test
    void shouldRestoreTagModeAndReportErrorWhenSearchFails() {
        Task task = task(1L);
        Project project = project(7L);
        when(taskService.get(1L)).thenReturn(task);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));

        when(meiliSearchService.searchImageIds("query", List.of()))
            .thenThrow(new SearchException("search failed", 500, "boom"));

        asyncService().run(1L, 7L, "query", List.of(), null);

        verify(taskService).updateTask(eq(task), eq(100), org.mockito.ArgumentMatchers.startsWith("Error:"));
        verify(taskService, never()).finishTask(task);
        verify(meiliSearchService, never()).addProjectToImages(any(), anyString());
        verify(imageInstanceService, never()).setTagMode(any());
    }

    @Test
    void shouldTagEarlierChunksWhenALaterImageFailsToCreate() {
        Task task = task(1L);
        Project project = project(7L);
        when(project.getName()).thenReturn("MyProject");
        when(taskService.get(1L)).thenReturn(task);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));

        Set<Long> ids = LongStream.rangeClosed(1, 101).boxed().collect(Collectors.toSet());
        when(meiliSearchService.searchImageIds("query", List.of())).thenReturn(ids);
        when(imageInstanceRepository.findAllByBaseImageIdInAndProject(any(), eq(project))).thenReturn(List.of());
        when(imageInstanceService.add(any(JsonObject.class))).thenAnswer(invocation -> {
            JsonObject json = invocation.getArgument(0);
            long baseImage = ((Number) json.get("baseImage")).longValue();
            if (baseImage == 101L) {
                throw new RuntimeException("boom");
            }
            CommandResponse response = new CommandResponse();
            response.setObject(imageInstanceWithBaseImage(baseImage));
            response.setStatus(200);
            return response;
        });

        asyncService().run(1L, 7L, "query", List.of(), null);

        verify(meiliSearchService).addProjectToImages(argThat(list -> list.size() == 100), eq("MyProject"));
        verify(taskService).updateTask(eq(task), eq(100), org.mockito.ArgumentMatchers.startsWith("Error:"));
        verify(taskService, never()).finishTask(task);
        verify(imageInstanceService).setTagMode(ImageInstanceService.TagMode.DEFER);
        verify(imageInstanceService).setTagMode(ImageInstanceService.TagMode.NORMAL);
    }

    @Test
    void shouldReportWarningWhenMetadataTaggingFails() {
        Task task = task(1L);
        Project project = project(7L);
        when(project.getName()).thenReturn("MyProject");
        when(taskService.get(1L)).thenReturn(task);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));

        when(meiliSearchService.searchImageIds("query", List.of())).thenReturn(Set.of(10L, 11L));
        when(imageInstanceRepository.findAllByBaseImageIdInAndProject(any(), eq(project))).thenReturn(List.of());
        when(imageInstanceService.add(any(JsonObject.class))).thenAnswer(invocation -> {
            CommandResponse response = new CommandResponse();
            JsonObject json = invocation.getArgument(0);
            response.setObject(imageInstanceWithBaseImage(((Number) json.get("baseImage")).longValue()));
            response.setStatus(200);
            return response;
        });
        when(meiliSearchService.addProjectToImages(any(), anyString()))
            .thenThrow(new SearchException("meili down", 500, "boom"));

        asyncService().run(1L, 7L, "query", List.of(), null);

        verify(meiliSearchService).addProjectToImages(List.of(10L, 11L), "MyProject");
        verify(taskService).updateTask(
            eq(task), eq(93), org.mockito.ArgumentMatchers.startsWith("Warning:")
        );
        verify(taskService).finishTask(task);
    }

    @Test
    void shouldDoNothingWhenTaskOrProjectIsMissing() {
        when(taskService.get(99L)).thenReturn(null);

        asyncService().run(99L, 7L, "query", List.of(), null);

        verify(meiliSearchService, never()).searchImageIds(any(), any());
    }

    @Test
    void shouldForwardIncomingAuthorizationHeadersWhileRunning() {
        Task task = task(1L);
        Project project = project(7L);
        when(project.getName()).thenReturn("MyProject");
        when(taskService.get(1L)).thenReturn(task);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));

        IncomingAuthorizationContext.Headers headers =
            new IncomingAuthorizationContext.Headers("Bearer token", "2026-09-23", "md5", "application/json");

        when(meiliSearchService.searchImageIds("query", List.of())).thenAnswer(invocation -> {
            assertEquals(headers, IncomingAuthorizationContext.get().orElse(null),
                "headers should be restored in the async thread for outbound repository calls");
            return Set.of(10L);
        });
        when(imageInstanceService.add(any(JsonObject.class))).thenAnswer(invocation -> {
            CommandResponse response = new CommandResponse();
            JsonObject json = invocation.getArgument(0);
            response.setObject(imageInstanceWithBaseImage(((Number) json.get("baseImage")).longValue()));
            response.setStatus(200);
            return response;
        });

        asyncService().run(1L, 7L, "query", List.of(), headers);

        assertTrue(IncomingAuthorizationContext.get().isEmpty(), "headers should be cleared after the job runs");
    }
}