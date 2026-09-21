package be.cytomine.service.project;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.domain.project.Project;
import be.cytomine.dto.project.ProjectFromSearchRequest;
import be.cytomine.dto.project.ProjectFromSearchResponse;
import be.cytomine.dto.project.ProjectFromSearchResponse.ProjectReference;
import be.cytomine.dto.project.ProjectFromSearchResponse.TaskReference;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.utils.TaskService;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectFromSearchService {

    public static final String ONTOLOGY_MODE_NEW = "NEW";
    public static final String ONTOLOGY_MODE_EXISTING = "EXISTING";

    private final ProjectService projectService;

    private final OntologyHttpContract ontologyHttpContract;

    private final TaskService taskService;

    private final CurrentUserService currentUserService;

    private final ProjectFromSearchAsyncService projectFromSearchAsyncService;

    /**
     * Creates the project (with the requested ontology), starts a task and schedules the async job that actually
     * imports the images. Returns a {@link ProjectFromSearchResponse} so the caller can poll the task without ever
     * receiving the image list.
     */
    public ProjectFromSearchResponse createAndSchedule(ProjectFromSearchRequest request) {
        long userId = currentUserService.getCurrentUser().id();
        String name = request.name();
        if (name == null || name.isBlank()) {
            throw new WrongArgumentException("Project name is required");
        }
        Long ontologyId = resolveOntology(userId, name, request.ontologyMode(), request.ontologyId());

        JsonObject projectJson = new JsonObject();
        projectJson.put("name", name);
        if (ontologyId != null) {
            projectJson.put("ontology", ontologyId);
        }
        CommandResponse projectResponse = projectService.add(projectJson);
        Project project = (Project) projectResponse.getObject();

        Task task = taskService.createNewTask(project, userId, false);

        List<String> filters = request.filters() != null ? request.filters() : List.of();
        projectFromSearchAsyncService.run(task.getId(), project.getId(), request.query(), filters);

        return new ProjectFromSearchResponse(
            new ProjectReference(project.getId(), project.getName()),
            new TaskReference(
                task.getId(), task.getProgress(), task.getProjectIdent(), task.getUserIdent(), task.isPrintInActivity()
            )
        );
    }

    private Long resolveOntology(long userId, String projectName, String ontologyMode, Long ontologyId) {
        if (ONTOLOGY_MODE_NEW.equals(ontologyMode)) {
            return ontologyHttpContract.create(userId, new CreateOntology(projectName))
                .map(HttpCommandResponse::data)
                .map(ApplyCommandResponse::id)
                .orElseThrow(() -> new WrongArgumentException("Could not create ontology '" + projectName + "'"));
        }
        if (ONTOLOGY_MODE_EXISTING.equals(ontologyMode)) {
            if (ontologyId == null) {
                throw new WrongArgumentException("Ontology id is required when using an existing ontology");
            }
            if (ontologyHttpContract.getLight(ontologyId, userId).isEmpty()) {
                throw new WrongArgumentException("Ontology " + ontologyId + " not found");
            }
            return ontologyId;
        }
        return null;
    }
}