package be.cytomine.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.mapper.BaseMapper;
import be.cytomine.domain.security.SecRole;

@Mapper(componentModel = "spring", uses = BaseMapper.class)
public interface RoleMapper {

    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"callBack", "version"})
    RoleResponse map(SecRole secRole);



}
