package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.StorageEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.StorageCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;
import be.cytomine.common.mapper.BaseMapper;

@Mapper(componentModel = "spring", uses = {be.cytomine.common.mapper.BaseMapper.class})
public interface StorageMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    StorageResponse mapToStorageResponse(StorageEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    StorageEntity mapToStorageEntity(CreateStorage createStorage, long userId, Timestamp creationDate);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "userId"})
    StorageCommandPayload mapToStorageCommandPayload(StorageEntity entity);

    @Mapping(target = "name", source = "newName")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"name", "updated"})
    StorageEntity update(StorageEntity entity, String newName, Timestamp now);

    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated"})
    StorageEntity updateWithPayload(StorageEntity entity, StorageCommandPayload replace, Timestamp now);
}
