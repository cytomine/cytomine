package org.cytomine.repository.mapper;


import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.cytomine.repository.persistence.entity.RelationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.RelationResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.OntologyUser;

@Mapper(componentModel = "spring", uses = {TermMapper.class, BaseMapper.class})
public interface OntologyMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    RelationResponse mapRelationResponse(RelationEntity relationEntity);

    OntologyUser map(Long id, String fullName);

    @Mapping(target = "user", source = "user")
    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "userId"})
    OntologyResponse mapToOntologyResponse(OntologyEntity ontologyEntity, long user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "updated", source = "creationDate")
    @Mapping(target = "terms", ignore = true)
    OntologyEntity mapToOntologyEntity(CreateOntology createOntology, long userId, Timestamp creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "userId", "terms"})
    OntologyCommandPayload mapToOntologyCommandPayload(OntologyEntity ontologyEntity);

    @Mapping(target = "name", source = "newName")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"name", "updated"})
    OntologyEntity update(OntologyEntity entity, String newName, Timestamp now);

    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated"})
    OntologyEntity updateWithPayload(OntologyEntity entity, OntologyCommandPayload replace, Timestamp now);


}
