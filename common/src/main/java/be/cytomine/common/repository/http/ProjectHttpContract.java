package be.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import be.cytomine.common.repository.model.project.payload.CreateProject;
import be.cytomine.common.repository.model.project.payload.UpdateProject;

@HttpExchange(ProjectHttpContract.ROOT_PATH)
public interface ProjectHttpContract {
    String ROOT_PATH = "/project";

    @PostExchange
    Optional<HttpCommandResponse> create(
        @Valid @RequestBody CreateProject payload
    );

    @GetExchange("/{id}")
    Optional<ProjectResponse> read(@PathVariable long id);

    @PutExchange("/{id}")
    Optional<HttpCommandResponse> update(
        @PathVariable long id,
        @RequestBody UpdateProject payload
    );

    @DeleteExchange("/{id}")
    Optional<HttpCommandResponse> delete(@PathVariable long id);
}
