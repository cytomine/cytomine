package be.cytomine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.domain.ontology.Ontology;

@Mapper(componentModel = "spring", uses = {TermMapper.class})
public interface OntologyMapper {

    @Mapping(target = "user", source = "user.id")
    OntologyResponse map(Ontology ontology);

    default Optional<Instant> map(Date value) {
        return Optional.ofNullable(value).map(Date::toInstant);
    }

    default Optional<Instant> map(LocalDateTime value) {
        return Optional.ofNullable(value).map(ldt -> ldt.toInstant(ZoneOffset.UTC));
    }
}
