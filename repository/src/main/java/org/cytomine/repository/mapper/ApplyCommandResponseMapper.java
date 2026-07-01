package org.cytomine.repository.mapper;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;

@Mapper(componentModel = "spring")
public interface ApplyCommandResponseMapper {

    default ApplyCommandResponse setDeleteTime(ApplyCommandResponse applyCommandResponse,
        Optional<LocalDateTime> deleteTime) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setDeleteTime(or, deleteTime);
            case TermResponse tr -> setDeleteTime(tr, deleteTime);
            default -> null;
        };
    }

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    TermResponse setDeleteTime(TermResponse or, Optional<LocalDateTime> deleteTime);

    @Mapping(target = "deleted", source = "deleteTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"deleted", "dataType"})
    OntologyResponse setDeleteTime(OntologyResponse or, Optional<LocalDateTime> deleteTime);

    default ApplyCommandResponse setUpdateTime(ApplyCommandResponse applyCommandResponse,
        Optional<LocalDateTime> updateTime) {
        return switch (applyCommandResponse) {
            case OntologyResponse or -> setUpdateTime(or, updateTime);
            case TermResponse tr -> setUpdateTime(tr, updateTime);
            default -> null;
        };
    }

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    TermResponse setUpdateTime(TermResponse or, Optional<LocalDateTime> updateTime);

    @Mapping(target = "updated", source = "updateTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated", "dataType"})
    OntologyResponse setUpdateTime(OntologyResponse or, Optional<LocalDateTime> updateTime);

}
