package be.cytomine;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.domain.ontology.Ontology;

@Mapper(componentModel = "spring", uses = {TermMapper.class})
public interface OntologyMapper {

    @Mapping(target = "user", source = "user.id")
    OntologyResponse map(Ontology ontology);
}
