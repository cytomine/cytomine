package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.ProjectRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.CurrentUserService;
import org.cytomine.repository.service.ProjectCommandService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.ProjectHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import be.cytomine.common.repository.model.list.filtered.BoundedFilter;
import be.cytomine.common.repository.model.list.filtered.ProjectListAvailableFiltersPayload;
import be.cytomine.common.repository.model.list.filtered.ProjectListFilteredResponse;
import be.cytomine.common.repository.model.project.payload.CreateProject;
import be.cytomine.common.repository.model.project.payload.UpdateProject;

import static java.time.temporal.ChronoUnit.MICROS;

@RequiredArgsConstructor
@RestController
@RequestMapping(ProjectHttpContract.ROOT_PATH)
public class ProjectController implements ProjectHttpContract {
    private final ACLService aclService;
    private final CurrentUserService currentUserService;
    private final ProjectCommandService service;
    private final ProjectRepository repository;

    private static Long min(Optional<BoundedFilter> bounds) {
        return bounds.flatMap(BoundedFilter::min).orElse(null);
    }

    private static Long max(Optional<BoundedFilter> bounds) {
        return bounds.flatMap(BoundedFilter::max).orElse(null);
    }

    @Override
    public Optional<HttpCommandResponse> create(@RequestBody CreateProject payload) {
        return service.create(currentUserService.getCurrentUserId(), payload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<ProjectResponse> read(@PathVariable long id) {
        long userId = currentUserService.getCurrentUserId();
        return repository.findByIdAndDeletedNull(id)
            .filter(project -> aclService.canReadProject(userId, project.getId())).map(service::mapToResponse);
    }

    @Override
    public Optional<HttpCommandResponse> update(@PathVariable long id, @RequestBody UpdateProject payload) {
        return service.update(currentUserService.getCurrentUserId(), id, payload,
            LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(@PathVariable long id) {
        return service.delete(currentUserService.getCurrentUserId(), id, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public ProjectListFilteredResponse listFiltered(Pageable pageable, ProjectListAvailableFiltersPayload filters) {
        long userId = currentUserService.getCurrentUserId();
        return new ProjectListFilteredResponse(
            repository.findAllReadableByUser(userId, pageable, filters.ontologyNames(), filters.roleNames(),
                    filters.tagLabels(),
                    min(filters.memberCount()), max(filters.memberCount()),
                    min(filters.imageCount()), max(filters.imageCount()),
                    min(filters.manualAnnotationCount()), max(filters.manualAnnotationCount()),
                    min(filters.reviewedAnnotationCount()), max(filters.reviewedAnnotationCount()))
                .map(service::mapToResponse), service.mapFilters(repository.getAvailableFilters()));
    }
}
