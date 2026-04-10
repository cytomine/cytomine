package org.cytomine.repository.mapper;

import java.util.List;
import java.util.Optional;

import org.cytomine.repository.persistence.entity.ReviewedAnnotationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.ReviewedAnnotationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.ReviewedAnnotationResponse;

@Mapper(componentModel = "spring")
public interface ReviewedAnnotationMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    @Mapping(target = "termIds", source = "termIds")
    ReviewedAnnotationResponse mapToResponse(ReviewedAnnotationEntity entity, List<Long> termIds);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    @Mapping(target = "termIds", source = "termIds")
    ReviewedAnnotationCommandPayload mapToCommandPayload(ReviewedAnnotationEntity entity, List<Long> termIds);

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }
}
