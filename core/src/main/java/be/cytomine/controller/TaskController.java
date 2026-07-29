package be.cytomine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.UrlApi;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.utils.TaskService;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TaskController extends RestCytomineController {

    private final CurrentUserService currentUserService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final UrlApi urlApi;

    @GetMapping("/task/{id}.json")
    public ResponseEntity<String> show(@PathVariable Long id) {
        Task task = taskService.get(id);
        if (task == null) {
            throw new ObjectNotFoundException("Task", id);
        }
        JsonObject jsonObject = task.toJsonObject(urlApi);
        jsonObject.put("comments", taskService.getLastComments(task, 5));
        return responseSuccess(jsonObject);
    }

    @PostMapping("/task.json")
    public ResponseEntity<String> add(@RequestBody JsonObject json) {
        Project project = null;
        try {
            project = projectService.get(json.getJSONAttrLong("project", 0L));
        } catch (Exception ignored) {
            // TODO
        }
        User user = currentUserService.getCurrentUser();
        boolean printInActivity = json.getJSONAttrBoolean("printInActivity", false);
        Task task = taskService.createNewTask(project, user, printInActivity);
        JsonObject jsonObject = task.toJsonObject(urlApi);
        jsonObject.put("comments", taskService.getLastComments(task, 5));
        return responseSuccess(JsonObject.of("task", jsonObject));
    }

    @GetMapping("/project/{project}/task/comment.json")
    public ResponseEntity<String> listCommentByProject(@PathVariable(value = "project") Long projectId) {
        Project project = projectService.find(projectId)
            .orElseThrow(() -> new ObjectNotFoundException("Project", projectId));
        return responseSuccess(taskService.listLastComments(project));
    }
}
