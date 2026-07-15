package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.TagEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.TagResponse;

@Mapper(componentModel = "spring", uses = {BaseMapper.class})
public interface TagMapper {

    @Mapping(target = "name", source = "newName")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"name", "updated"})
    TagEntity update(TagEntity entity, String newName, Timestamp now);

    @Mapping(target = "creatorName", source = "creatorName")
    @BeanMapping(ignoreUnmappedSourceProperties = {"userId", "version"})
    TagResponse mapToTagResponse(TagEntity entity, String creatorName);
}
