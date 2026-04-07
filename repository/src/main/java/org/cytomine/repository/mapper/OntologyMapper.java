package org.cytomine.repository.mapper;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.term.TermCommandPayload;
import be.cytomine.common.repository.model.term.payload.CreateTerm;
import be.cytomine.common.repository.model.term.payload.TermResponse;

@Mapper(componentModel = "spring")
public interface OntologyMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    TermResponse map(TermEntity termEntity);

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

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "children", "deleted"})
    @Mapping(target = "ontology", source = "ontologyId")
    @Mapping(target = "parent", ignore = true)
    TermCommandPayload mapToTermCommandPayload(TermEntity termEntity);

    default Optional<LocalDateTime> date(LocalDateTime zonedDateTime) {
        return Optional.ofNullable(zonedDateTime);
    }
}
