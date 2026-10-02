package be.cytomine.controller.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.common.repository.http.ProjectHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.project.payload.CreateProject;
import be.cytomine.common.repository.model.project.payload.UpdateProject;
import be.cytomine.domain.project.Project;
import be.cytomine.mapper.ProjectMapper;
import be.cytomine.utils.JsonObject;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ProjectController {
    public static final String UNABLE_TO_FIND_PROJECT = "Unable to find project with id: %s";

    private final ProjectHttpContract projectHttpContract;
    private final ProjectMapper projectMapper;

    @GetMapping("/project/{id}.json")
    public JsonObject read(@PathVariable long id) {
        log.debug("GET /project/{}.json", id);
        return projectHttpContract.read(id)
            .map(projectMapper::map)
            .map(Project::getDataFromDomain)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_PROJECT, id)));
    }

    @PostMapping("/project.json")
    public Optional<HttpCommandResponse> create(@RequestBody CreateProject payload) {
        log.debug("POST /project.json - {}", payload);
        return projectHttpContract.create(payload);
    }

    @PutMapping("/project/{id}.json")
    public HttpCommandResponse update(@PathVariable long id, @RequestBody UpdateProject payload) {
        log.debug("PUT /project/{}.json - {}", id, payload);
        return projectHttpContract.update(id, payload)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_PROJECT, id)));
    }

    @DeleteMapping("/project/{id}.json")
    public HttpCommandResponse delete(@PathVariable long id) {
        log.debug("DELETE /project/{}.json", id);
        return projectHttpContract.delete(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, format(UNABLE_TO_FIND_PROJECT, id)));
    }
}
