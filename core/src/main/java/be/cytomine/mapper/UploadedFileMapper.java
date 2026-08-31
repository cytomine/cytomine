package be.cytomine.mapper;

import java.util.Optional;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;

@Mapper(componentModel = "spring")
public interface UploadedFileMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"thumbnailUrl", "dataType"})
    @Mapping(target = "id", source = "response.id")
    @Mapping(target = "user", source = "response.user")
    @Mapping(target = "parent", source = "response.parent")
    @Mapping(target = "storage", source = "response.storage")
    @Mapping(target = "filename", source = "response.filename")
    @Mapping(target = "originalFilename", source = "response.originalFilename")
    @Mapping(target = "ext", source = "response.ext")
    @Mapping(target = "contentType", source = "response.contentType")
    @Mapping(target = "size", source = "response.size")
    @Mapping(target = "path", source = "response.path")
    @Mapping(target = "status", source = "response.status")
    @Mapping(target = "projects", source = "response.projects")
    @Mapping(target = "created", source = "response.created")
    @Mapping(target = "updated", source = "response.updated")
    @Mapping(target = "deleted", source = "response.deleted")
    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl")
    UploadedFileResponse withThumbnailUrl(UploadedFileResponse response, Optional<String> thumbnailUrl);
}
