package be.cytomine.service.project;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.OntologyLight;
import be.cytomine.domain.project.Project;
import be.cytomine.dto.project.ProjectFromSearchRequest;
import be.cytomine.dto.project.ProjectFromSearchResponse;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.utils.TaskService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectFromSearchServiceTests {

    @Mock
    private ProjectService projectService;

    @Mock
    private OntologyHttpContract ontologyHttpContract;

    @Mock
    private TaskService taskService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ProjectFromSearchAsyncService projectFromSearchAsyncService;

    private ProjectFromSearchService service() {
        return new ProjectFromSearchService(
            projectService, ontologyHttpContract, taskService, currentUserService,
            projectFromSearchAsyncService
        );
    }

    private CommandResponse projectResponse(long projectId, String name) {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        when(project.getName()).thenReturn(name);
        CommandResponse response = new CommandResponse();
        response.setStatus(200);
        response.setObject(project);
        return response;
    }

    private Task task(long id) {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn((Long) id);
        return task;
    }

    private void stubCurrentUser(long id) {
        UserResponse user = mock(UserResponse.class);
        when(user.id()).thenReturn(id);
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void shouldCreateProjectFromSearchWithNewOntology() {
        stubCurrentUser(5L);
        OntologyResponse ontology = new OntologyResponse(
            "MyProject", 42L, new HashSet<>(), LocalDateTime.now(), Optional.empty(), Optional.empty(), 5L
        );
        HttpCommandResponse command =
            new HttpCommandResponse(false, ontology, null, "Add", Set.of());
        when(ontologyHttpContract.create(eq(5L), any(CreateOntology.class))).thenReturn(Optional.of(command));
        CommandResponse projectResponse = projectResponse(9L, "MyProject");
        when(projectService.add(any(JsonObject.class))).thenReturn(projectResponse);
        Task task = task(77L);
        when(taskService.createNewTask(any(Project.class), eq(5L), eq(false))).thenReturn(task);

        ProjectFromSearchRequest request =
            new ProjectFromSearchRequest("MyProject", "NEW", null, "nucleus", List.of("channel=red"));

        ProjectFromSearchResponse result = service().createAndSchedule(request);

        ArgumentCaptor<JsonObject> projectJson = ArgumentCaptor.forClass(JsonObject.class);
        verify(projectService).add(projectJson.capture());
        assertEquals("MyProject", projectJson.getValue().get("name"));
        assertEquals(42L, projectJson.getValue().get("ontology"));
        verify(projectFromSearchAsyncService).run(77L, 9L, "nucleus", List.of("channel=red"));

        assertEquals(9L, result.project().id());
        assertEquals("MyProject", result.project().name());
        assertEquals(77L, result.task().id());
    }

    @Test
    void shouldUseExistingOntologyWithoutCreatingOne() {
        stubCurrentUser(5L);
        OntologyLight light = mock(OntologyLight.class);
        when(ontologyHttpContract.getLight(33L, 5L)).thenReturn(Optional.of(light));
        CommandResponse projectResponse = projectResponse(9L, "MyProject");
        when(projectService.add(any(JsonObject.class))).thenReturn(projectResponse);
        Task task = task(77L);
        when(taskService.createNewTask(any(Project.class), eq(5L), eq(false))).thenReturn(task);

        ProjectFromSearchRequest request = new ProjectFromSearchRequest("MyProject", "EXISTING", 33L, null, null);

        service().createAndSchedule(request);

        verify(ontologyHttpContract, never()).create(eq(5L), any(CreateOntology.class));
        ArgumentCaptor<JsonObject> projectJson = ArgumentCaptor.forClass(JsonObject.class);
        verify(projectService).add(projectJson.capture());
        assertEquals(33L, projectJson.getValue().get("ontology"));
    }

    @Test
    void shouldCreateProjectWithoutOntologyByDefault() {
        stubCurrentUser(5L);
        CommandResponse projectResponse = projectResponse(9L, "MyProject");
        when(projectService.add(any(JsonObject.class))).thenReturn(projectResponse);
        Task task = task(77L);
        when(taskService.createNewTask(any(Project.class), eq(5L), eq(false))).thenReturn(task);

        ProjectFromSearchRequest request = new ProjectFromSearchRequest("MyProject", null, null, null, null);

        service().createAndSchedule(request);

        verifyNoInteractions(ontologyHttpContract);
        ArgumentCaptor<JsonObject> projectJson = ArgumentCaptor.forClass(JsonObject.class);
        verify(projectService).add(projectJson.capture());
        assertEquals("MyProject", projectJson.getValue().get("name"));
        assertTrue(!projectJson.getValue().containsKey("ontology"));
    }

    @Test
    void shouldRejectMissingProjectName() {
        stubCurrentUser(5L);

        ProjectFromSearchRequest request = new ProjectFromSearchRequest(null, null, null, null, null);

        assertThrows(WrongArgumentException.class, () -> service().createAndSchedule(request));
        verifyNoInteractions(ontologyHttpContract);
        verifyNoInteractions(projectService);
    }

    @Test
    void shouldRejectExistingOntologyWhenIdOrContentIsMissing() {
        stubCurrentUser(5L);

        ProjectFromSearchRequest withoutId = new ProjectFromSearchRequest("MyProject", "EXISTING", null, null, null);
        assertThrows(WrongArgumentException.class, () -> service().createAndSchedule(withoutId));

        ProjectFromSearchRequest notFound = new ProjectFromSearchRequest("MyProject", "EXISTING", 33L, null, null);
        when(ontologyHttpContract.getLight(33L, 5L)).thenReturn(Optional.empty());
        assertThrows(WrongArgumentException.class, () -> service().createAndSchedule(notFound));
    }

    @Test
    void shouldPropagateDuplicateProjectName() {
        stubCurrentUser(5L);
        when(projectService.add(any(JsonObject.class))).thenThrow(new AlreadyExistException("Project already exists"));

        ProjectFromSearchRequest request = new ProjectFromSearchRequest("MyProject", null, null, null, null);

        assertThrows(AlreadyExistException.class, () -> service().createAndSchedule(request));
        verifyNoInteractions(ontologyHttpContract);
    }
}