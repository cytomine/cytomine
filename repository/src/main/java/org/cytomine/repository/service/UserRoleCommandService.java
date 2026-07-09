package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.UserRoleMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.UserRoleRepository;
import org.cytomine.repository.persistence.entity.UserRoleEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.UserRoleCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateUserRoleCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteUserRoleCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateUserRoleCommand;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;
import be.cytomine.common.repository.model.userrole.payload.role.payload.UpdateUserRole;

@Component
@RequiredArgsConstructor
@Getter
public class UserRoleCommandService implements
    CRUDCommandService<CreateUserRole, UpdateUserRole, UserRoleCommandPayload, UserRoleEntity, UserRoleResponse> {

    private final ACLService aclService;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserRoleRepository userRoleRepository;
    @Setter
    private ApplyCommandService applyCommandService;

    @Override
    public UserRoleEntity save(UserRoleEntity entity) {
        return userRoleRepository.save(entity);
    }

    @Override
    public Optional<UserRoleEntity> get(long id) {
        return userRoleRepository.findById(id);
    }

    @Override
    public UserRoleEntity mapCreateToEntity(CreateUserRole createPayload, long userId, Timestamp creationDate) {
        return userRoleMapper.mapToUserRoleEntity(createPayload, creationDate);
    }

    @Override
    public UserRoleCommandPayload map(UserRoleEntity entity) {
        return userRoleMapper.mapToUserRoleCommandPayload(entity);
    }

    @Override
    public UserRoleResponse mapToResponse(UserRoleEntity entity) {
        return userRoleMapper.mapToUserRoleResponse(entity);
    }

    @Override
    public UserRoleEntity updateEntityWithEntity(UserRoleEntity entity, UpdateUserRole payload, Timestamp now) {
        return userRoleMapper.update(entity, payload.roleId().orElse(entity.getSecRoleId()), now);
    }

    @Override
    public UserRoleEntity updateEntityWithPayload(
            UserRoleEntity entity, UserRoleCommandPayload payload, Timestamp now) {
        return userRoleMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public CreateCommandRequest<UserRoleCommandPayload> mapCreateCommand(long userId, UserRoleCommandPayload after) {
        return new CreateUserRoleCommand(after, userId);
    }

    @Override
    public UpdateCommandRequest<UserRoleCommandPayload> mapUpdateCommand(long userId, UserRoleCommandPayload before,
        UserRoleCommandPayload after) {
        return new UpdateUserRoleCommand(before, after, userId);
    }

    @Override
    public DeleteCommandRequest<UserRoleCommandPayload> mapDeleteCommand(long userId, UserRoleCommandPayload before) {
        return new DeleteUserRoleCommand(before, userId);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return aclService.canWriteUserRole(userId);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return aclService.canDeleteUserRole(userId);
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return aclService.canWriteUserRole(userId);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return aclService.canDeleteUserRole(userId);
    }
}
