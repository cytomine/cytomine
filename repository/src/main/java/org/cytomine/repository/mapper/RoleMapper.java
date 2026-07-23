package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.RoleEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.RoleCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.role.payload.CreateRole;

@Mapper(componentModel = "spring", uses = {be.cytomine.common.mapper.BaseMapper.class})
public interface RoleMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    RoleResponse mapToRoleResponse(RoleEntity entity);

    @Mapping(target = "version", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"dataType"})
    RoleEntity mapToRoleEntity(RoleResponse roleResponse);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    RoleEntity mapToRoleEntity(CreateRole createRole, Timestamp creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    RoleCommandPayload mapToRoleCommandPayload(RoleEntity entity);

    @Mapping(target = "authority", source = "newAuthority")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"authority", "updated"})
    RoleEntity update(RoleEntity entity, String newAuthority, Timestamp now);

    @Mapping(target = "authority", source = "replace.authority")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated"})
    RoleEntity updateWithPayload(RoleEntity entity, RoleCommandPayload replace, Timestamp now);
}
