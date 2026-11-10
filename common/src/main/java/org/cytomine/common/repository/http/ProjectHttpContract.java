package org.cytomine.common.repository.http;

import java.util.Optional;

import jakarta.validation.Valid;
import org.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import org.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import org.cytomine.common.repository.model.list.filtered.ProjectListFilteredResponse;
import org.cytomine.common.repository.model.list.filtered.ProjectListFiltersPayload;
import org.cytomine.common.repository.model.project.payload.CreateProject;
import org.cytomine.common.repository.model.project.payload.UpdateProject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(ProjectHttpContract.ROOT_PATH)
public interface ProjectHttpContract {
    String ROOT_PATH = "/projects";

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

    @PostExchange("search")
    ProjectListFilteredResponse listFiltered(Pageable pageable,
        @RequestBody Optional<ProjectListFiltersPayload> filters);
}
