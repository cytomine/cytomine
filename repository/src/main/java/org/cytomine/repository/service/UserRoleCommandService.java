package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.UserRoleEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.UserRoleCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
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
    @Setter
    private ApplyCommandService applyCommandService;

    @Override
    public UserRoleEntity updateEntityWithEntity(UserRoleEntity entity, UpdateUserRole payload, Timestamp now) {
        return null;
    }

    @Override
    public UserRoleEntity updateEntityWithPayload(UserRoleEntity entity, UserRoleCommandPayload payload,
        Timestamp now) {
        return null;
    }

    @Override
    public UserRoleResponse mapToResponse(UserRoleEntity entity) {
        return null;
    }

    @Override
    public UserRoleEntity mapCreateToEntity(CreateUserRole createPayload, long userId, Timestamp creationDate) {
        return null;
    }

    @Override
    public UserRoleCommandPayload map(UserRoleEntity entity) {
        return null;
    }

    @Override
    public UserRoleEntity save(UserRoleEntity entity) {
        return null;
    }

    @Override
    public UpdateCommandRequest<UserRoleCommandPayload> mapUpdateCommand(long userId, UserRoleCommandPayload before,
        UserRoleCommandPayload after) {
        return null;
    }

    @Override
    public CreateCommandRequest<UserRoleCommandPayload> mapCreateCommand(long userId, UserRoleCommandPayload after) {
        return null;
    }

    @Override
    public DeleteCommandRequest<UserRoleCommandPayload> mapDeleteCommand(long userId, UserRoleCommandPayload before) {
        return null;
    }

    @Override
    public Optional<UserRoleEntity> get(long id) {
        return Optional.empty();
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return false;
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return false;
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return false;
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return false;
    }
}
