package org.cytomine.repository.mapper;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.cytomine.repository.persistence.entity.TermRelationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.TermCommandPayload;
import be.cytomine.common.repository.model.command.payload.request.TermRelationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;

@Mapper(componentModel = "spring")
public interface OntologyMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    TermResponse map(TermEntity termEntity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "term2IdOntologyId"})
    @Mapping(target = "ontologyId", source = "term1IdOntologyId")
    TermRelationResponse mapToTermRelationResponse(TermRelationEntity termRelationEntity);

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "name", source = "createTerm.name")
    @Mapping(target = "color", source = "createTerm.color")
    @Mapping(target = "ontologyId", source = "createTerm.ontology")
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "updated", source = "creationDate")
    TermEntity map(CreateTerm createTerm, LocalDateTime creationDate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "term1IdOntologyId", ignore = true)
    @Mapping(target = "term2IdOntologyId", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "updated", source = "creationDate")
    TermRelationEntity mapToTermRelationEntity(CreateTermRelation createTermRelation, LocalDateTime creationDate, long relationId);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "children", "deleted"})
    @Mapping(target = "ontology", source = "ontologyId")
    @Mapping(target = "parent", ignore = true)
    TermCommandPayload mapToTermCommandPayload(TermEntity termEntity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version","term2IdOntologyId"})
    @Mapping(target = "ontologyId", source = "term1IdOntologyId")
    TermRelationCommandPayload mapToTermRelationCommandPayload(TermRelationEntity termRelationEntity);

    default Optional<LocalDateTime> date(LocalDateTime zonedDateTime) {
        return Optional.ofNullable(zonedDateTime);
    }
}
