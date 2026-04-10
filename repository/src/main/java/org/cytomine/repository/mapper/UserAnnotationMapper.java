package org.cytomine.repository.mapper;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.UserAnnotationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.command.payload.request.UserAnnotationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserAnnotationResponse;

@Mapper(componentModel = "spring")
public interface UserAnnotationMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    UserAnnotationResponse mapToResponse(UserAnnotationEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "countReviewedAnnotations"})
    UserAnnotationCommandPayload mapToCommandPayload(UserAnnotationEntity entity);

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }
}
