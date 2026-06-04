package org.cytomine.repository.mapper;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.cytomine.repository.persistence.entity.RelationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.RelationResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;

@Mapper(componentModel = "spring")
public interface OntologyMapper {

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }

    default LocalDateTime mapTimestamp(Timestamp value) {
        return value.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }

    default Optional<LocalDateTime> mapToLocalDateTime(Timestamp value) {
        return Optional.ofNullable(value).map(this::mapTimestamp);
    }

    default Timestamp map(LocalDateTime value) {
        return Timestamp.valueOf(value);
    }

    default <T> T map(Optional<T> t) {
        return t.orElse(null);
    }


    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    RelationResponse mapRelationResponse(RelationEntity relationEntity);



    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "userId"})
    OntologyResponse mapToOntologyResponse(OntologyEntity ontologyEntity);

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
    OntologyEntity update(OntologyEntity entity, String newName, Timestamp now);

    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "updated", source = "now")
    OntologyEntity updateWithPayload(OntologyEntity entity, OntologyCommandPayload replace, Timestamp now);


}
