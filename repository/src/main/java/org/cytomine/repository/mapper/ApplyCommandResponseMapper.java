package org.cytomine.repository.mapper;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.command.payload.response.UndoCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;

@Mapper(componentModel = "spring")
public interface ApplyCommandResponseMapper {

    default ApplyCommandResponse setDeleteTime(
        ApplyCommandResponse applyCommandResponse,
        Optional<Instant> deleteTime
    ) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setDeleteTimeOR(or, deleteTime);
            case TermResponse tr -> setDeleteTimeTR(tr, deleteTime);
            case StorageResponse sr -> setDeleteTimeSR(sr, deleteTime);
            case TermRelationResponse trr -> setDeleteTimeTRR(trr, deleteTime);
            case TagDomainAssociationResponse tdar -> setDeleteTimeTDAR(tdar, deleteTime);
            case UndoCommandResponse ucr -> ucr;
            case UploadedFileResponse ufr -> setDeleteTimeUFR(ufr, deleteTime);
        };
    }

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TermResponse setDeleteTimeTR(TermResponse or, Optional<Instant> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @Mapping(target = "terms", expression = "java(setDeleteTimes(or.terms(), deleteTime))")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType", "terms"})
    OntologyResponse setDeleteTimeOR(OntologyResponse or, Optional<Instant> deleteTime);


    default Set<TermResponse> setDeleteTimes(Set<TermResponse> terms, Optional<Instant> deleteTime) {
        return terms.stream().map(term -> setDeleteTimeTR(term, deleteTime)).collect(Collectors.toSet());
    }

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    StorageResponse setDeleteTimeSR(StorageResponse or, Optional<Instant> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TermRelationResponse setDeleteTimeTRR(TermRelationResponse or, Optional<Instant> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TagDomainAssociationResponse setDeleteTimeTDAR(TagDomainAssociationResponse or, Optional<Instant> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    UploadedFileResponse setDeleteTimeUFR(UploadedFileResponse or, Optional<Instant> deleteTime);

    default ApplyCommandResponse setUpdateTime(
        ApplyCommandResponse applyCommandResponse,
        Optional<Instant> updateTime
    ) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setUpdateTimeOR(or, updateTime);
            case TermResponse tr -> setUpdateTimeTR(tr, updateTime);
            case StorageResponse sr -> setUpdateTimeSR(sr, updateTime);
            case TermRelationResponse trr -> setUpdateTimeTRR(trr, updateTime);
            case TagDomainAssociationResponse tdar -> setUpdateTimeTDAR(tdar, updateTime);
            case UndoCommandResponse ucr -> ucr;
            case UploadedFileResponse ufr -> setUpdateTimeUFR(ufr, updateTime);
        };
    }

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TermResponse setUpdateTimeTR(TermResponse or, Optional<Instant> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @Mapping(target = "terms", expression = "java(setUpdateTimes(or.terms(), updateTime))")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType", "terms"})
    OntologyResponse setUpdateTimeOR(OntologyResponse or, Optional<Instant> updateTime);

    default Set<TermResponse> setUpdateTimes(Set<TermResponse> terms, Optional<Instant> updateTime) {
        return terms.stream().map(term -> setUpdateTimeTR(term, updateTime)).collect(Collectors.toSet());
    }

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TermRelationResponse setUpdateTimeTRR(TermRelationResponse or, Optional<Instant> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    StorageResponse setUpdateTimeSR(StorageResponse or, Optional<Instant> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TagDomainAssociationResponse setUpdateTimeTDAR(TagDomainAssociationResponse or, Optional<Instant> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    UploadedFileResponse setUpdateTimeUFR(UploadedFileResponse or, Optional<Instant> updateTime);

}
