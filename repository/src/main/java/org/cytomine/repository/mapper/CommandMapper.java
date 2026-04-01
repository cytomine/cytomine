package org.cytomine.repository.mapper;

import java.time.ZonedDateTime;

import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.CommandResponse;
import be.cytomine.common.repository.model.command.CommandV2Request;

@Mapper(componentModel = "spring")
public interface CommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "data", source = "commandV2Request")
    CommandV2Entity map(CommandV2Request<?> commandV2Request, ZonedDateTime created, ZonedDateTime updated,
                        long userId);

    default CommandResponse<?> map(CommandV2Entity commandV2Entity) {
        return new CommandResponse<>(commandV2Entity.getId(), commandV2Entity.getData());
    }
}
