package org.cytomine.repository.mapper;


import org.cytomine.repository.persistence.entity.RelationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.command.payload.response.RelationResponse;

@Mapper(componentModel = "spring", uses = {TermMapper.class, BaseMapper.class})
public interface OntologyMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    RelationResponse mapRelationResponse(RelationEntity relationEntity);

}
