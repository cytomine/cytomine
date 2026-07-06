package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.UserCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;

@Mapper(componentModel = "spring", uses = {BaseMapper.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "firstname", ignore = true)
    @Mapping(target = "lastname", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "isDeveloper", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountExpired", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "passwordExpired", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "publicKey", ignore = true)
    @Mapping(target = "privateKey", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    UserEntity mapToUserEntity(CreateUser createUser, long userId, Timestamp creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "name", "reference", "firstname", "lastname", "email",
        "language", "isDeveloper", "userId", "password", "enabled", "accountExpired", "accountLocked",
        "passwordExpired", "origin", "publicKey", "privateKey", "created", "updated", "deleted"})
    UserCommandPayload mapToUserCommandPayload(UserEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "username", "name", "reference", "firstname", "lastname",
        "email", "language", "isDeveloper", "userId", "password", "enabled", "accountExpired", "accountLocked",
        "passwordExpired", "origin", "publicKey", "privateKey"})
    UserResponse mapToUserResponse(UserEntity entity);

    @Mapping(target = "username", source = "newUsername")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"username", "updated"})
    UserEntity update(UserEntity entity, String newUsername, Timestamp now);

    @Mapping(target = "username", source = "replace.username")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "version", source = "entity.version")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated"})
    UserEntity updateWithPayload(UserEntity entity, UserCommandPayload replace, Timestamp now);
}
