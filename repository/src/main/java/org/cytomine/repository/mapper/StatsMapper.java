package org.cytomine.repository.mapper;

import org.cytomine.common.repository.model.stat.payload.FlatStatUserTerm;
import org.cytomine.common.repository.model.stat.payload.StatPerTermAndImage;
import org.cytomine.common.repository.model.stat.payload.StatTerm;
import org.cytomine.repository.persistence.projection.StatPerTermAndImageProjection;
import org.cytomine.repository.persistence.projection.StatTermProjection;
import org.cytomine.repository.persistence.projection.StatUserTermProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    StatTerm map(StatTermProjection statTermProjection);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "term.id", source = "termId")
    @Mapping(target = "term.name", source = "termName")
    @Mapping(target = "term.color", source = "termColor")
    @Mapping(target = "term.count", source = "termCount")
    FlatStatUserTerm map(StatUserTermProjection statUserTermProjection);

    StatPerTermAndImage map(StatPerTermAndImageProjection statPerTermAndImageProjection);

}
