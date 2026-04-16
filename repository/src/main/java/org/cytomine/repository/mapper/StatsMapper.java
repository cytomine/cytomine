package org.cytomine.repository.mapper;

import org.cytomine.repository.persistence.StatTermProjection;
import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.stat.payload.StatTerm;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    StatTerm map(StatTermProjection statTermProjection);

}
