package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.UserMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.RoleRepository;
import org.cytomine.repository.persistence.UserRepository;
import org.cytomine.repository.persistence.entity.RoleEntity;
import org.cytomine.repository.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.UserCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateUserCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteUserCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateUserCommand;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.common.repository.model.user.payload.UpdateUser;

import static java.lang.String.format;

@RequiredArgsConstructor
@Component
@Getter
public class UserCommandService
    implements CRUDCommandService<CreateUser, UpdateUser, UserCommandPayload, UserEntity, UserResponse> {
    private final ACLService aclService;
    private final CommandV2Repository commandV2Repository;
    private final UserRepository userRepository;
    private final CommandMapper commandMapper;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Override
    public UserEntity updateEntityWithEntity(UserEntity entity, UpdateUser payload, Timestamp now) {
        String entityLanguage = entity.getLanguage() != null ? entity.getLanguage().name() : null;
        return userMapper.update(entity, payload.email().orElse(entity.getEmail()),
            payload.name().orElse(entity.getName()),
            payload.firstname().orElse(entity.getFirstname()), payload.lastname().orElse(entity.getLastname()),
            payload.language().orElse(entityLanguage), now);
    }

    @Override
    public UserEntity updateEntityWithPayload(UserEntity entity, UserCommandPayload payload, Timestamp now) {
        return userMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public UserResponse mapToResponse(UserEntity entity) {
        return userMapper.mapToUserResponse(entity);
    }

    @Override
    public UserEntity mapCreateToEntity(CreateUser createPayload, long userId, Timestamp creationDate) {
        Optional<RoleEntity> maybeRole = roleRepository.findByAuthorityAndDeletedNull(createPayload.role());
        RoleEntity roleEntity = maybeRole.orElseThrow(
            () -> new IllegalArgumentException(format("Role not found %s", createPayload.role())));
        return userMapper.mapToUserEntity(createPayload, userId, creationDate, Set.of(roleEntity));
    }

    @Override
    public UserCommandPayload map(UserEntity entity) {
        return userMapper.mapToUserCommandPayload(entity);
    }

    @Override
    public UserEntity save(UserEntity entity) {
        return userRepository.save(entity);
    }

    @Override
    public UpdateCommandRequest<UserCommandPayload> mapUpdateCommand(long userId, UserCommandPayload before,
        UserCommandPayload after) {
        return new UpdateUserCommand(before, after, userId);
    }

    @Override
    public CreateCommandRequest<UserCommandPayload> mapCreateCommand(long userId, UserCommandPayload after) {
        return new CreateUserCommand(after, userId);
    }

    @Override
    public DeleteCommandRequest<UserCommandPayload> mapDeleteCommand(long userId, UserCommandPayload before) {
        return new DeleteUserCommand(before, userId);
    }

    @Override
    public Optional<UserEntity> get(long id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return aclService.isAdmin(userId);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return aclService.isAdmin(userId);
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return aclService.isAdmin(userId);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return aclService.isAdmin(userId);
    }
}
