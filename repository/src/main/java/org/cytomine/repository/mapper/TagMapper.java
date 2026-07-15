package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.TagEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.TagCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TagResponse;
import be.cytomine.common.repository.model.tag.payload.CreateTag;

@Mapper(componentModel = "spring", uses = {BaseMapper.class})
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    TagEntity mapToTagEntity(CreateTag createTag, long userId, Timestamp creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"userId", "version"})
    TagCommandPayload mapToCommandPayload(TagEntity entity);

    @Mapping(target = "name", source = "newName")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"name", "updated"})
    TagEntity update(TagEntity entity, String newName, Timestamp now);

    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated"})
    TagEntity updateWithPayload(TagEntity entity, TagCommandPayload replace, Timestamp now);

    @Mapping(target = "creatorName", source = "creatorName")
    @BeanMapping(ignoreUnmappedSourceProperties = {"userId", "version"})
    TagResponse mapToTagResponse(TagEntity entity, String creatorName);
}
