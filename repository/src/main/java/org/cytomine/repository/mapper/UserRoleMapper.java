package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.UserRoleEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.UserRoleCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;
import be.cytomine.common.repository.model.userrole.payload.role.payload.CreateUserRole;

@Mapper(componentModel = "spring", uses = {be.cytomine.common.mapper.BaseMapper.class})
public interface UserRoleMapper {
    @Mapping(target = "deleted", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted"})
    UserRoleEntity delete(UserRoleEntity userRoleEntity, Timestamp now);

    @Mapping(target = "userId", source = "secUserId")
    @Mapping(target = "roleId", source = "secRoleId")
    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    UserRoleResponse mapToUserRoleResponse(UserRoleEntity entity);

    @Mapping(target = "userId", source = "secUserId")
    @Mapping(target = "roleId", source = "secRoleId")
    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    UserRoleCommandPayload mapToUserRoleCommandPayload(UserRoleEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "secUserId", source = "createUserRole.userId")
    @Mapping(target = "secRoleId", source = "createUserRole.roleId")
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    UserRoleEntity mapToUserRoleEntity(CreateUserRole createUserRole, Timestamp creationDate);

    @Mapping(target = "secRoleId", source = "newRoleId")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"secRoleId", "updated"})
    UserRoleEntity update(UserRoleEntity entity, long newRoleId, Timestamp now);

    @Mapping(target = "secUserId", source = "replace.userId")
    @Mapping(target = "secRoleId", source = "replace.roleId")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "secRoleId", "secUserId"})
    UserRoleEntity updateWithPayload(UserRoleEntity entity, UserRoleCommandPayload replace, Timestamp now);
}
