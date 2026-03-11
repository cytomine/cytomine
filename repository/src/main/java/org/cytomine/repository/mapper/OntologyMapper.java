package org.cytomine.repository.mapper;

import org.cytomine.repository.persistence.entity.OntologyEntity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.Ontology;
import be.cytomine.common.repository.model.Term;

@Mapper(componentModel = "spring")
public interface OntologyMapper {

    Ontology map(OntologyEntity ontologyEntity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    Term map(TermEntity termEntity);

}
