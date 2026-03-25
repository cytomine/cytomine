package org.cytomine.repository.mapper;

import java.util.Date;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.OntologyResponse;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.TermCommandPayload;

@Mapper(componentModel = "spring")
public interface OntologyMapper {
    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    OntologyResponse map(OntologyEntity ontologyEntity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    TermResponse map(TermEntity termEntity);


    @Mapping(target = "children", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "name", source = "createTerm.name")
    @Mapping(target = "color", source = "createTerm.color")
    @Mapping(target = "ontologyId", source = "createTerm.ontology")
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", source = "creationDate")
    TermEntity map(CreateTerm createTerm, Date creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "children"})
    @Mapping(target = "ontology", source = "ontologyId")
    @Mapping(target = "parent", ignore = true)
    TermCommandPayload mapToTermCommandPayload(TermEntity termEntity);


}
