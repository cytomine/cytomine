package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.persistence.ProjectListRow;
import org.cytomine.repository.persistence.ProjectRepository;
import org.cytomine.repository.persistence.projection.ProjectRoleProjection;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.CurrentUserService;
import org.cytomine.repository.service.ProjectCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.ProjectHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.ProjectListRowResponse;
import be.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import be.cytomine.common.repository.model.list.filtered.OptionallyBoundedFilter;
import be.cytomine.common.repository.model.list.filtered.ProjectListFilteredResponse;
import be.cytomine.common.repository.model.list.filtered.ProjectListFiltersPayload;
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

    private Long min(Optional<OptionallyBoundedFilter> bounds) {
        return bounds.flatMap(OptionallyBoundedFilter::min).orElse(null);
    }

    private Long max(Optional<OptionallyBoundedFilter> bounds) {
        return bounds.flatMap(OptionallyBoundedFilter::max).orElse(null);
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
    public ProjectListFilteredResponse listFiltered(Pageable pageable,
        @RequestBody Optional<ProjectListFiltersPayload> filters) {
        long userId = currentUserService.getCurrentUserId();
        ProjectListFiltersPayload existingFilters = filters.orElseGet(ProjectListFiltersPayload::empty);

        Page<ProjectListRow> rows =
            repository.findAllReadableByUser(userId, pageable, existingFilters.ontologyNames(),
                existingFilters.roleNames(), existingFilters.tagLabels(), existingFilters.name().orElse(null),
                min(existingFilters.memberCount()),
                max(existingFilters.memberCount()), min(existingFilters.imageCount()),
                max(existingFilters.imageCount()), min(existingFilters.manualAnnotationCount()),
                max(existingFilters.manualAnnotationCount()), min(existingFilters.reviewedAnnotationCount()),
                max(existingFilters.reviewedAnnotationCount()));
        Set<Long> projectIds = rows.stream().map(ProjectListRow::id).collect(Collectors.toSet());
        Map<Long, Set<String>> rolesPerProject = projectIds.isEmpty() ? Map.of()
            : repository.findRolesForProjectIds(userId, projectIds).stream()
                .collect(Collectors.groupingBy(ProjectRoleProjection::getProjectId,
                    Collectors.mapping(ProjectRoleProjection::getRole, Collectors.toSet())));

        Page<ProjectListRowResponse> responses =
            new PageImpl<>(rows.stream()
                .map(r -> service.mapToResponse(r, rolesPerProject.getOrDefault(r.id(), Set.of()))).toList(),
                pageable, rows.getTotalElements());

        return new ProjectListFilteredResponse(responses, service.mapFilters(repository.getAvailableFilters()));
    }
}
