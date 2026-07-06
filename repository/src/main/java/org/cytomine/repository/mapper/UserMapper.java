package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

import be.cytomine.common.repository.model.command.payload.request.UserCommandPayload;

@Mapper(componentModel = "spring", uses = {BaseMapper.class})

public interface UserMapper {

    UserEntity updateWithPayload(UserEntity entity, UserCommandPayload replace, Timestamp now);


}
