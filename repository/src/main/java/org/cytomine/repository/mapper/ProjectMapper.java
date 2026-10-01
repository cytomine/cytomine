package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.ProjectEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.mapper.BaseMapper;
import be.cytomine.common.repository.model.command.payload.request.ProjectCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.ProjectResponse;
import be.cytomine.common.repository.model.project.payload.CreateProject;

@Mapper(componentModel = "spring", uses = {BaseMapper.class})
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "countAnnotations", ignore = true)
    @Mapping(target = "countImages", ignore = true)
    @Mapping(target = "countJobAnnotations", ignore = true)
    @Mapping(target = "countReviewedAnnotations", ignore = true)
    @Mapping(target = "name", source = "createProject.name")
    @Mapping(target = "ontologyId", source = "createProject.ontology")
    @Mapping(target = "blindMode", source = "createProject.blindMode")
    @Mapping(target = "areImagesDownloadable", source = "createProject.areImagesDownloadable")
    @Mapping(target = "closed", source = "createProject.isClosed")
    @Mapping(target = "hideUsersLayers", source = "createProject.hideUsersLayers")
    @Mapping(target = "hideAdminsLayers", source = "createProject.hideAdminsLayers")
    @Mapping(target = "mode", source = "mode")
    @BeanMapping(ignoreUnmappedSourceProperties = {"isRestricted", "isReadOnly"})
    ProjectEntity mapToProjectEntity(CreateProject createProject, String mode, Timestamp creationDate);

    @Mapping(target = "isClosed", source = "closed")
    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "countAnnotations", "countImages",
        "countJobAnnotations", "countReviewedAnnotations"})
    ProjectCommandPayload mapToCommandPayload(ProjectEntity entity);

    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "ontologyId", source = "replace.ontologyId")
    @Mapping(target = "blindMode", source = "replace.blindMode")
    @Mapping(target = "areImagesDownloadable", source = "replace.areImagesDownloadable")
    @Mapping(target = "closed", source = "replace.isClosed")
    @Mapping(target = "hideUsersLayers", source = "replace.hideUsersLayers")
    @Mapping(target = "hideAdminsLayers", source = "replace.hideAdminsLayers")
    @Mapping(target = "mode", source = "replace.mode")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"closed", "updated"})
    ProjectEntity updateWithPayload(ProjectEntity entity, ProjectCommandPayload replace, Timestamp now);

    @Mapping(target = "ontology", source = "entity.ontologyId")
    @Mapping(target = "ontologyName", source = "ontologyName")
    @Mapping(target = "numberOfImages", source = "entity.countImages")
    @Mapping(target = "numberOfAnnotations", source = "entity.countAnnotations")
    @Mapping(target = "numberOfJobAnnotations", source = "entity.countJobAnnotations")
    @Mapping(target = "numberOfReviewedAnnotations", source = "entity.countReviewedAnnotations")
    @Mapping(target = "isClosed", source = "entity.closed")
    @Mapping(target = "isReadOnly", source = "isReadOnly")
    @Mapping(target = "isRestricted", source = "isRestricted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "mode"})
    ProjectResponse mapToProjectResponse(ProjectEntity entity, String ontologyName, boolean isReadOnly,
                                         boolean isRestricted);
}
