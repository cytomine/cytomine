package be.cytomine;

import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.domain.ontology.Term;

@Mapper
public interface TermMapper {


    TermResponse map(Term term);
}
