package org.cytomine.repository.mapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.payload.response.UndoCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.command.payload.response.UserRoleResponse;

@Mapper(componentModel = "spring")
public interface ApplyCommandResponseMapper {

    default ApplyCommandResponse setDeleteTime(ApplyCommandResponse applyCommandResponse,
        Optional<LocalDateTime> deleteTime) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setDeleteTimeOR(or, deleteTime);
            case RoleResponse rr -> setDeleteTimeRR(rr, deleteTime);
            case TermResponse tr -> setDeleteTimeTR(tr, deleteTime);
            case StorageResponse sr -> setDeleteTimeSR(sr, deleteTime);
            case TermRelationResponse trr -> setDeleteTimeTRR(trr, deleteTime);
            case TagDomainAssociationResponse tdar -> setDeleteTimeTDAR(tdar, deleteTime);
            case UndoCommandResponse ucr -> ucr;
            case UploadedFileResponse ufr -> setDeleteTimeUFR(ufr, deleteTime);
            case UserResponse ur -> setDeleteTimeUR(ur, deleteTime);
            case UserRoleResponse urr -> setDeleteTimeUFR(urr, deleteTime);
        };
    }

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TermResponse setDeleteTimeTR(TermResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @Mapping(target = "terms", expression = "java(setDeleteTimes(or.terms(), deleteTime))")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType", "terms"})
    OntologyResponse setDeleteTimeOR(OntologyResponse or, Optional<LocalDateTime> deleteTime);

    default Set<TermResponse> setDeleteTimes(Set<TermResponse> terms, Optional<LocalDateTime> deleteTime) {
        return terms.stream().map(term -> setDeleteTimeTR(term, deleteTime)).collect(Collectors.toSet());
    }

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    RoleResponse setDeleteTimeRR(RoleResponse rr, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    StorageResponse setDeleteTimeSR(StorageResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TermRelationResponse setDeleteTimeTRR(TermRelationResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TagDomainAssociationResponse setDeleteTimeTDAR(TagDomainAssociationResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    UploadedFileResponse setDeleteTimeUFR(UploadedFileResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    UserRoleResponse setDeleteTimeUFR(UserRoleResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    UserResponse setDeleteTimeUR(UserResponse or, Optional<LocalDateTime> deleteTime);

    default ApplyCommandResponse setUpdateTime(ApplyCommandResponse applyCommandResponse,
        Optional<LocalDateTime> updateTime) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setUpdateTimeOR(or, updateTime);
            case RoleResponse rr -> setUpdateTimeRR(rr, updateTime);
            case TermResponse tr -> setUpdateTimeTR(tr, updateTime);
            case StorageResponse sr -> setUpdateTimeSR(sr, updateTime);
            case TermRelationResponse trr -> setUpdateTimeTRR(trr, updateTime);
            case TagDomainAssociationResponse tdar -> setUpdateTimeTDAR(tdar, updateTime);
            case UndoCommandResponse ucr -> ucr;
            case UploadedFileResponse ufr -> setUpdateTimeUFR(ufr, updateTime);
            case UserRoleResponse urr -> setUpdateTimeUFR(urr, updateTime);
            case UserResponse ur -> setUpdateTimeUR(ur, updateTime);
        };
    }

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TermResponse setUpdateTimeTR(TermResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @Mapping(target = "terms", expression = "java(setUpdateTimes(or.terms(), updateTime))")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType", "terms"})
    OntologyResponse setUpdateTimeOR(OntologyResponse or, Optional<LocalDateTime> updateTime);

    default Set<TermResponse> setUpdateTimes(Set<TermResponse> terms, Optional<LocalDateTime> updateTime) {
        return terms.stream().map(term -> setUpdateTimeTR(term, updateTime)).collect(Collectors.toSet());
    }

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    RoleResponse setUpdateTimeRR(RoleResponse rr, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TermRelationResponse setUpdateTimeTRR(TermRelationResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    StorageResponse setUpdateTimeSR(StorageResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TagDomainAssociationResponse setUpdateTimeTDAR(TagDomainAssociationResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    UploadedFileResponse setUpdateTimeUFR(UploadedFileResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    UserRoleResponse setUpdateTimeUFR(UserRoleResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    UserResponse setUpdateTimeUR(UserResponse or, Optional<LocalDateTime> updateTime);

}
