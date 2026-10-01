package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.ProjectMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.OntologyRepository;
import org.cytomine.repository.persistence.ProjectRepository;
import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.cytomine.repository.persistence.entity.ProjectEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.ProjectCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateProjectCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteProjectCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateProjectCommand;
import be.cytomine.common.repository.model.project.payload.CreateProject;
import be.cytomine.common.repository.model.project.payload.UpdateProject;

@Component
@RequiredArgsConstructor
@Getter
public class ProjectCommandService
    implements CRUDCommandService<CreateProject, UpdateProject, ProjectCommandPayload, ProjectEntity, ProjectResponse> {
    private static final String MODE_CLASSIC = "CLASSIC";
    private static final String MODE_READ_ONLY = "READ_ONLY";
    private static final String MODE_RESTRICTED = "RESTRICTED";

    private final ACLService aclService;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final OntologyRepository ontologyRepository;
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;

    @Override
    public ProjectEntity updateEntityWithEntity(ProjectEntity entity, UpdateProject payload, Timestamp now) {
        payload.name().ifPresent(entity::setName);
        payload.ontology().ifPresent(entity::setOntologyId);
        payload.blindMode().ifPresent(entity::setBlindMode);
        payload.areImagesDownloadable().ifPresent(entity::setAreImagesDownloadable);
        payload.isClosed().ifPresent(entity::setClosed);
        payload.hideUsersLayers().ifPresent(entity::setHideUsersLayers);
        payload.hideAdminsLayers().ifPresent(entity::setHideAdminsLayers);
        if (payload.isReadOnly().isPresent() || payload.isRestricted().isPresent()) {
            entity.setMode(toMode(payload.isReadOnly().orElse(false), payload.isRestricted().orElse(false)));
        }
        entity.setUpdated(now);
        return entity;
    }

    @Override
    public ProjectEntity updateEntityWithPayload(ProjectEntity entity, ProjectCommandPayload payload, Timestamp now) {
        return projectMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public ProjectResponse mapToResponse(ProjectEntity entity) {
        String ontologyName = entity.getOntologyId() == null ? null
            : ontologyRepository.findById(entity.getOntologyId())
                .map(OntologyEntity::getName)
                .orElse(null);
        return projectMapper.mapToProjectResponse(
            entity,
            ontologyName,
            MODE_READ_ONLY.equals(entity.getMode()),
            MODE_RESTRICTED.equals(entity.getMode())
        );
    }

    @Override
    public ProjectEntity mapCreateToEntity(CreateProject createPayload, long userId, Timestamp creationDate) {
        return projectMapper.mapToProjectEntity(
            createPayload,
            toMode(createPayload.isReadOnly(), createPayload.isRestricted()),
            creationDate
        );
    }

    @Override
    public ProjectCommandPayload map(ProjectEntity entity) {
        return projectMapper.mapToCommandPayload(entity);
    }

    @Override
    public ProjectEntity save(ProjectEntity entity) {
        return projectRepository.save(entity);
    }

    @Override
    public UpdateCommandRequest<ProjectCommandPayload> mapUpdateCommand(
        long userId,
        ProjectCommandPayload before,
        ProjectCommandPayload after
    ) {
        return new UpdateProjectCommand(before, after, userId);
    }

    @Override
    public CreateCommandRequest<ProjectCommandPayload> mapCreateCommand(long userId, ProjectCommandPayload after) {
        return new CreateProjectCommand(after, userId);
    }

    @Override
    public DeleteCommandRequest<ProjectCommandPayload> mapDeleteCommand(long userId, ProjectCommandPayload before) {
        return new DeleteProjectCommand(before, userId);
    }

    @Override
    public Optional<ProjectEntity> get(long id) {
        return projectRepository.findById(id);
    }

    @Override
    public void afterCreate(long userId, ProjectCommandPayload payload) {
        aclService.grantProjectOwnerPermission(userId, payload.id());
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return aclService.canWriteProject(userId, id);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return aclService.canDeleteProject(userId, id);
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return canWriteId(userId, id);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return canDeleteId(userId, id);
    }

    private String toMode(boolean isReadOnly, boolean isRestricted) {
        if (isReadOnly) {
            return MODE_READ_ONLY;
        }
        if (isRestricted) {
            return MODE_RESTRICTED;
        }
        return MODE_CLASSIC;
    }
}
