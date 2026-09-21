package be.cytomine.service.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.domain.project.Project;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.UrlApi;
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

    private final UrlApi urlApi;

    /**
     * Creates the project (with the requested ontology), starts a task and schedules the async job that actually
     * imports the images. Returns {@code {project, task}} so the caller can poll the task without ever receiving the
     * image list.
     */
    public Map<String, Object> createAndSchedule(JsonObject json) {
        long userId = currentUserService.getCurrentUser().id();
        String name = json.getJSONAttrStr("name");
        if (name == null || name.isBlank()) {
            throw new WrongArgumentException("Project name is required");
        }
        Long ontologyId = resolveOntology(
            userId, name, json.getJSONAttrStr("ontologyMode", "NO"), json.getJSONAttrLong("ontologyId", null)
        );

        JsonObject projectJson = new JsonObject();
        projectJson.put("name", name);
        if (ontologyId != null) {
            projectJson.put("ontology", ontologyId);
        }
        CommandResponse projectResponse = projectService.add(projectJson);
        Project project = (Project) projectResponse.getObject();

        Task task = taskService.createNewTask(project, userId, false);

        String query = json.getJSONAttrStr("query", null);
        List<String> filters = filters(json);
        projectFromSearchAsyncService.run(task.getId(), project.getId(), query, filters);

        JsonObject projectData = new JsonObject();
        projectData.put("id", project.getId());
        projectData.put("name", project.getName());
        return Map.of("project", projectData, "task", task.toJsonObject(urlApi));
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

    private List<String> filters(JsonObject json) {
        Object filters = json.get("filters");
        if (filters instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return new ArrayList<>();
    }
}