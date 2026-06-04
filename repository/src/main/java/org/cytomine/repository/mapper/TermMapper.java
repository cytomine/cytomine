package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;

@Mapper(componentModel = "spring",uses = {BaseMapper.class})
public interface TermMapper {

    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "replace.deleted")
    @Mapping(target = "color", source = "replace.color")
    @Mapping(target = "comment", source = "replace.comment")
    @Mapping(target = "ontology", source = "replace.ontology")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "parent"})
    TermEntity updateWithPayload(TermEntity entity, TermCommandPayload replace, Timestamp now);

    @Mapping(target = "name", source = "newName")
    @Mapping(target = "color", source = "newColor")
    @Mapping(target = "updated", source = "now")
    TermEntity update(TermEntity entity, String newName, String newColor, Timestamp now);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "children", "deleted"})
    @Mapping(target = "ontology", source = "ontologyId")
    @Mapping(target = "parent", ignore = true)
    TermCommandPayload mapToTermCommandPayload(TermEntity termEntity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    @Mapping(target = "name", ignore = true)
    TermResponse mapToTermResponse(TermEntity termEntity);


    @Mapping(target = "children", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "name", source = "createTerm.name")
    @Mapping(target = "color", source = "createTerm.color")
    @Mapping(target = "ontologyId", source = "createTerm.ontology")
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "updated", source = "creationDate")
    TermEntity map(CreateTerm createTerm, Timestamp creationDate);
}
