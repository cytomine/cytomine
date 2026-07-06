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
    @Mapping(target = "firstname", ignore = true)
    @Mapping(target = "lastname", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    UserEntity mapToUserEntity(CreateUser createUser, long userId, Timestamp creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "firstname", "lastname", "created", "updated", "deleted"})
    UserCommandPayload mapToUserCommandPayload(UserEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "username", "firstname", "lastname"})
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
