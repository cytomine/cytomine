package org.cytomine.repository.mapper;

import java.time.LocalDateTime;

import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.CommandV2Response;
import be.cytomine.common.repository.model.command.request.CommandV2Request;

@Mapper(componentModel = "spring")
public interface CommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "data", source = "commandV2Request")
    CommandV2Entity map(CommandV2Request<?> commandV2Request, LocalDateTime created, LocalDateTime updated,
                        long userId);

    default CommandV2Response<?> map(CommandV2Entity commandV2Entity) {
        return new CommandV2Response<>(commandV2Entity.getId(), commandV2Entity.getData());
    }
}
