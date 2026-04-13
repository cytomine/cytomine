package org.cytomine.repository.mapper;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.AnnotationTermEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.command.payload.request.AnnotationTermCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.AnnotationTermResponse;

@Mapper(componentModel = "spring")
public interface AnnotationTermMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    AnnotationTermResponse mapToResponse(AnnotationTermEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    AnnotationTermCommandPayload mapToCommandPayload(AnnotationTermEntity entity);

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }
}
