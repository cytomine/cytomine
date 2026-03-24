package org.cytomine.repository.mapper;

import be.cytomine.common.repository.model.command.TermCommandPayload;
import be.cytomine.common.repository.model.command.delete.DeleteTermCommand;
import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.OntologyResponse;
import be.cytomine.common.repository.model.TermResponse;

@Mapper(componentModel = "spring")
public interface OntologyMapper {
    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    OntologyResponse map(OntologyEntity ontologyEntity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    @Mapping(target = "ontology", source = "ontologyId")
    TermResponse map(TermEntity termEntity);


    @Mapping(target = "children", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "ontologyId", source = "ontology")
    TermEntity map(CreateTerm createTerm);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "children"})
    @Mapping(target = "ontology", source = "ontologyId")
    @Mapping(target = "parent", ignore = true)
    TermCommandPayload mapToTermCommandPayload(TermEntity termEntity);
}
