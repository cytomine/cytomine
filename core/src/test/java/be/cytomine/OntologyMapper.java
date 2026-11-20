package be.cytomine;

import org.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.domain.ontology.Ontology;

@Mapper(componentModel = "spring", uses = {TermMapper.class})
public interface OntologyMapper {

    @Mapping(target = "user", source = "userId")
    OntologyResponse map(Ontology ontology);
}
