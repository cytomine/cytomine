package org.cytomine.repository.mapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.TermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.payload.term.TermCommandPayload;

@Mapper(componentModel = "spring")
public interface OntologyMapper {
    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    TermResponse map(TermEntity termEntity);


    default Optional<LocalDateTime> date(LocalDateTime zonedDateTime) {
        return
            Optional.ofNullable(zonedDateTime);
    }

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "name", source = "createTerm.name")
    @Mapping(target = "color", source = "createTerm.color")
    @Mapping(target = "ontologyId", source = "createTerm.ontology")
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "updated", source = "creationDate")
    TermEntity map(CreateTerm createTerm, Date creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "children", "deleted"})
    @Mapping(target = "ontology", source = "ontologyId")
    @Mapping(target = "parent", ignore = true)
    TermCommandPayload mapToTermCommandPayload(TermEntity termEntity);


    @Mapping(target = "version", ignore = true)
    @Mapping(target = "ontologyId", source = "ontology")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"parent"})
    TermEntity mapToTermEntity(TermCommandPayload termCommandPayload);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ontologyId", source = "ontology")
    @Mapping(target = "children", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"parent", "id"})
    @Mapping(target = "deleted", ignore = true)
    TermEntity mapToTermEntityWithoutID(TermCommandPayload termCommandPayload);
}
