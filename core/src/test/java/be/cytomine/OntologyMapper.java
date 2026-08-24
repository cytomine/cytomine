package be.cytomine;

import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.domain.ontology.Ontology;

@Mapper(componentModel = "spring", uses = {TermMapper.class})
public interface OntologyMapper {

    OntologyResponse map(Ontology ontology);
}
