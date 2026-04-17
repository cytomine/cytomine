package be.cytomine.service.stats;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.stat.payload.FlatStatUserTerm;
import be.cytomine.common.repository.model.stat.payload.StatUserTerm;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    default Set<StatUserTerm> mapToUserTerms(Set<FlatStatUserTerm> flatStatUserTerms) {
        return new LinkedHashSet<>(flatStatUserTerms.stream()
            .collect(Collectors.toMap(
                FlatStatUserTerm::userId,
                f -> new StatUserTerm(f.userId(), f.username(), new HashSet<>(List.of(f.term()))),
                (a, b) -> {
                    a.terms().addAll(b.terms());
                    return a;
                },
                LinkedHashMap::new
            ))
            .values());
    }
}
