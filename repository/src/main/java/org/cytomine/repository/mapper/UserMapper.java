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
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "now")
    UserEntity mapToUserEntity(CreateUser entity, long userId, Timestamp now);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "created", "updated", "deleted"})
    UserCommandPayload mapToUserCommandPayload(UserEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "created", "updated", "deleted"})
    UserResponse mapToUserResponse(UserEntity entity);

    @Mapping(target = "username", source = "newUsername")
    @Mapping(target = "email", source = "newEmail")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"username", "email", "updated"})
    UserEntity update(UserEntity entity, String newUsername, String newEmail, Timestamp now);

    @Mapping(target = "username", source = "replace.username")
    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "email", source = "replace.email")
    @Mapping(target = "firstname", source = "replace.firstname")
    @Mapping(target = "lastname", source = "replace.lastname")
    @Mapping(target = "language", source = "replace.language")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "version", source = "entity.version")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated"})
    UserEntity updateWithPayload(UserEntity entity, UserCommandPayload replace, Timestamp now);
}
