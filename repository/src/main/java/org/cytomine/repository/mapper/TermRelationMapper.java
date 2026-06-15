package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;

@Mapper(componentModel = "spring", uses = BaseMapper.class)
public interface TermRelationMapper {


    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    @Mapping(target = "name", ignore = true)
    TermRelationResponse mapToTermRelationResponse(TermRelationEntity termRelationEntity);


    @BeanMapping(ignoreUnmappedSourceProperties = {"name"})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "updated", ignore = true)
    TermRelationEntity mapToTermRelationEntity(CreateTermRelation createTermRelation,
        Timestamp creationDate,
        long relationId);


    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    @Mapping(target = "ontologyId", source = "ontologyId")
    @Mapping(target = "name", ignore = true)
    TermRelationCommandPayload mapToTermRelationCommandPayload(TermRelationEntity termRelationEntity, long ontologyId);

    @Mapping(target = "updated", source = "now")
    @Mapping(target = "term1Id", source = "newTerm1Id")
    @Mapping(target = "term2Id", source = "newTerm2Id")
    @BeanMapping(ignoreUnmappedSourceProperties = {"term1Id", "term2Id", "updated"})
    TermRelationEntity update(TermRelationEntity entity, long newTerm1Id, long newTerm2Id, Timestamp now);

    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "replace.created")
    @Mapping(target = "deleted", source = "replace.deleted")
    @Mapping(target = "relationId", source = "replace.relationId")
    @Mapping(target = "term1Id", source = "replace.term1Id")
    @Mapping(target = "term2Id", source = "replace.term2Id")
    @BeanMapping(ignoreUnmappedSourceProperties = {"ontologyId", "term2Id", "updated", "name"})
    TermRelationEntity updateTermRelationWithPayload(TermRelationEntity entity,
        TermRelationCommandPayload replace,
        Timestamp now);

}
