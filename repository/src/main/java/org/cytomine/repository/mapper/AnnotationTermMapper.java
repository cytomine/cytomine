package org.cytomine.repository.mapper;

import java.time.LocalDateTime;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.AnnotationTermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.annotationterm.payload.CreateAnnotationTerm;
import be.cytomine.common.repository.model.command.payload.request.AnnotationTermCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.AnnotationTermResponse;

@Mapper(componentModel = "spring")
public interface AnnotationTermMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    AnnotationTermResponse mapToResponse(AnnotationTermEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    AnnotationTermCommandPayload mapToCommandPayload(AnnotationTermEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", constant = "0L")
    @Mapping(target = "created", source = "now")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "deleted", ignore = true)
    AnnotationTermEntity mapToEntity(CreateAnnotationTerm payload, LocalDateTime now);

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }
}
