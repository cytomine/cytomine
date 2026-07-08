package org.cytomine.repository.mapper;

import org.cytomine.repository.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
@Mapper(componentModel = "spring", uses = {BaseMapper.class})
public interface RoleMapper {

    RoleResponse map(RoleEntity roleEntity);

}
