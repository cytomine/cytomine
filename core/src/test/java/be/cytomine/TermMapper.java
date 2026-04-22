package be.cytomine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.domain.ontology.Term;

@Mapper(componentModel = "spring")
public interface TermMapper {

    //@BeanMapping(ignoreUnmappedSourceProperties = {"children"})
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "ontologyId", source = "ontology.id")
    TermResponse map(Term term);

    default Optional<LocalDateTime> map(Date value) {
        return Optional.ofNullable(value)
            .map(d -> Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
    }
}
