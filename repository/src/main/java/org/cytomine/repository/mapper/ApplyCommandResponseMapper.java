package org.cytomine.repository.mapper;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;

@Mapper(componentModel = "spring")
public interface ApplyCommandResponseMapper {

    default ApplyCommandResponse setDeleteTime(ApplyCommandResponse applyCommandResponse,
        Optional<LocalDateTime> deleteTime) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setDeleteTimeOR(or, deleteTime);
            case TermResponse tr -> setDeleteTimeTR(tr, deleteTime);
            case StorageResponse sr -> setDeleteTimeSR(sr, deleteTime);
            case TermRelationResponse trr -> setDeleteTimeTRR(trr, deleteTime);
        };
    }

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TermResponse setDeleteTimeTR(TermResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    OntologyResponse setDeleteTimeOR(OntologyResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    StorageResponse setDeleteTimeSR(StorageResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TermRelationResponse setDeleteTimeTRR(TermRelationResponse or, Optional<LocalDateTime> deleteTime);

    default ApplyCommandResponse setUpdateTime(ApplyCommandResponse applyCommandResponse,
        Optional<LocalDateTime> updateTime) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setUpdateTimeOR(or, updateTime);
            case TermResponse tr -> setUpdateTimeTR(tr, updateTime);
            case StorageResponse sr -> setUpdateTimeSR(sr, updateTime);
            case TermRelationResponse trr -> setUpdateTimeTRR(trr, updateTime);
        };
    }

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TermResponse setUpdateTimeTR(TermResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    OntologyResponse setUpdateTimeOR(OntologyResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TermRelationResponse setUpdateTimeTRR(TermRelationResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    StorageResponse setUpdateTimeSR(StorageResponse or, Optional<LocalDateTime> updateTime);

}
