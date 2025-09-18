package be.cytomine.controller.meta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.repository.meta.TagRepository;
import be.cytomine.repository.project.ProjectRepository;
import be.cytomine.service.meta.TagService;
import be.cytomine.service.utils.TaskService;
import be.cytomine.utils.JsonObject;
import be.cytomine.utils.Task;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class RestTagController extends RestCytomineController {

    private final TagService tagService;

    private final TagRepository tagRepository;

    private final ProjectRepository projectRepository;

    private final TaskService taskService;

    @GetMapping("/tag.json")
    public ResponseEntity<String> list() {
        log.debug("REST request to list tags");
        return responseSuccess(tagService.list());
    }

    @GetMapping("/tag/{id}.json")
    public ResponseEntity<String> show(
        @PathVariable Long id
    ) {
        log.debug("REST request to get Tag : {}", id);
        return tagService.find(id)
            .map(this::responseSuccess)
            .orElseGet(() -> responseNotFound("Tag", id));
    }


    @PostMapping("/tag.json")
    public ResponseEntity<String> add(@RequestBody String json) {
        log.debug("REST request to save Tag : " + json);
        return add(tagService, json);
    }

    @PutMapping("/tag/{id}.json")
    public ResponseEntity<String> edit(@PathVariable String id, @RequestBody JsonObject json) {
        log.debug("REST request to edit Tag : " + id);
        return update(tagService, json);
    }

    @DeleteMapping("/tag/{id}.json")
    public ResponseEntity<String> delete(@PathVariable String id,
                                         @RequestParam(required = false) Long task) {
        log.debug("REST request to delete Tag : " + id);
        Task existingTask = taskService.get(task);
        return delete(tagService, JsonObject.of("id", id), existingTask);
    }
}
