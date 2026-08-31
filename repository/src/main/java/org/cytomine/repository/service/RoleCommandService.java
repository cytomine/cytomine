package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.RoleMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.RoleRepository;
import org.cytomine.repository.persistence.entity.RoleEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.RoleCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateRoleCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteRoleCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateRoleCommand;
import be.cytomine.common.repository.model.role.payload.CreateRole;
import be.cytomine.common.repository.model.role.payload.UpdateRole;

@Component
@RequiredArgsConstructor
@Getter
public class RoleCommandService
    implements CRUDCommandService<CreateRole, UpdateRole, RoleCommandPayload, RoleEntity, RoleResponse> {

    private final ACLService aclService;
    private final CommandMapper commandMapper;
    private final CommandV2Repository commandV2Repository;
    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    @Setter
    private ApplyCommandService applyCommandService;

    @Override
    public RoleEntity save(RoleEntity entity) {
        return roleRepository.save(entity);
    }

    @Override
    public Optional<RoleEntity> get(long id) {
        return roleRepository.findById(id);
    }

    @Override
    public RoleEntity mapCreateToEntity(CreateRole createPayload, long userId, Timestamp creationDate) {
        return roleMapper.mapToRoleEntity(createPayload, creationDate);
    }

    @Override
    public RoleCommandPayload map(RoleEntity entity) {
        return roleMapper.mapToRoleCommandPayload(entity);
    }

    @Override
    public RoleResponse mapToResponse(RoleEntity entity) {
        return roleMapper.mapToRoleResponse(entity);
    }

    @Override
    public RoleEntity updateEntityWithEntity(RoleEntity entity, UpdateRole payload, Timestamp now) {
        return roleMapper.update(entity, payload.authority().orElse(entity.getAuthority()), now);
    }

    @Override
    public RoleEntity updateEntityWithPayload(RoleEntity entity, RoleCommandPayload payload, Timestamp now) {
        return roleMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public CreateCommandRequest<RoleCommandPayload> mapCreateCommand(long userId, RoleCommandPayload after) {
        return new CreateRoleCommand(after, userId);
    }

    @Override
    public UpdateCommandRequest<RoleCommandPayload> mapUpdateCommand(
        long userId,
        RoleCommandPayload before,
        RoleCommandPayload after
    ) {
        return new UpdateRoleCommand(before, after, userId);
    }

    @Override
    public DeleteCommandRequest<RoleCommandPayload> mapDeleteCommand(long userId, RoleCommandPayload before) {
        return new DeleteRoleCommand(before, userId);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return aclService.canWriteRole(userId);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return aclService.canDeleteRole(userId);
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return aclService.canWriteRole(userId);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return aclService.canDeleteRole(userId);
    }
}
