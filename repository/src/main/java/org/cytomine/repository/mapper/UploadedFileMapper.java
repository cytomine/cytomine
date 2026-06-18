package org.cytomine.repository.mapper;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.cytomine.repository.persistence.entity.UploadedFileEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import be.cytomine.common.repository.model.command.payload.request.UploadedFileCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;

@Mapper(componentModel = "spring", uses = {BaseMapper.class})
public interface UploadedFileMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "lTree"})
    @Mapping(target = "user", source = "userId", qualifiedByName = "wrapLong")
    @Mapping(target = "parent", source = "parent", qualifiedByName = "mapParentToId")
    @Mapping(target = "storage", source = "storageId", qualifiedByName = "wrapLong")
    @Mapping(target = "path", expression = "java(entity.getFilename())")
    @Mapping(target = "statusText", expression = "java(String.valueOf(entity.getStatus()))")
    @Mapping(target = "projects", source = "projects", qualifiedByName = "mapArrayToSet")
    UploadedFileResponse mapToUploadedFileResponse(UploadedFileEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "lTree", "userId"})
    @Mapping(target = "parent", source = "parent", qualifiedByName = "mapParentToId")
    @Mapping(target = "projects", source = "projects", qualifiedByName = "mapArrayToSet")
    UploadedFileCommandPayload mapToUploadedFileCommandPayload(UploadedFileEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "userId", source = "createPayload.user")
    @Mapping(target = "storageId", source = "createPayload.storage")
    @Mapping(target = "parent", source = "createPayload.parent", qualifiedByName = "mapIdToParent")
    @Mapping(target = "projects", source = "createPayload.projects", qualifiedByName = "mapOptionalSetToArray")
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "lTree", ignore = true)
    UploadedFileEntity mapToUploadedFileEntity(CreateUploadedFile createPayload, Timestamp creationDate);

    @Mapping(target = "filename", source = "replace.filename")
    @Mapping(target = "originalFilename", source = "replace.originalFilename")
    @Mapping(target = "ext", source = "replace.ext")
    @Mapping(target = "contentType", source = "replace.contentType")
    @Mapping(target = "size", source = "replace.size")
    @Mapping(target = "status", source = "replace.status")
    @Mapping(target = "storageId", source = "replace.storageId")
    @Mapping(target = "parent", source = "replace.parent", qualifiedByName = "mapIdToParent")
    @Mapping(target = "projects", source = "replace.projects", qualifiedByName = "mapSetToArray")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "userId", source = "entity.userId")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @Mapping(target = "lTree", source = "entity.lTree")
    @Mapping(target = "version", source = "entity.version")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "deleted"})
    UploadedFileEntity updateWithPayload(UploadedFileEntity entity, UploadedFileCommandPayload replace, Timestamp now);

    @Named("wrapLong")
    default Optional<Long> wrapLong(Long value) {
        return Optional.ofNullable(value);
    }

    @Named("mapParentToId")
    default Optional<Long> mapParentToId(UploadedFileEntity parent) {
        return Optional.ofNullable(parent).map(UploadedFileEntity::getId);
    }

    @Named("mapIdToParent")
    default UploadedFileEntity mapIdToParent(Optional<Long> parentId) {
        if (parentId == null || parentId.isEmpty()) {
            return null;
        }
        UploadedFileEntity parent = new UploadedFileEntity();
        parent.setId(parentId.get());
        return parent;
    }

    @Named("mapArrayToSet")
    default Set<Long> mapArrayToSet(Long[] projects) {
        if (projects == null) {
            return Set.of();
        }
        return Arrays.stream(projects).collect(Collectors.toSet());
    }

    @Named("mapOptionalSetToArray")
    default Long[] mapOptionalSetToArray(Optional<Set<Long>> projects) {
        return projects.map(set -> set.toArray(Long[]::new)).orElse(null);
    }

    @Named("mapSetToArray")
    default Long[] mapSetToArray(Set<Long> projects) {
        return projects != null ? projects.toArray(Long[]::new) : null;
    }
}
