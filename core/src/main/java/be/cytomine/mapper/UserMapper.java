package be.cytomine.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.mapper.BaseMapper;
import be.cytomine.domain.security.User;

@Mapper(componentModel = "spring", uses = {BaseMapper.class, RoleMapper.class})
public interface UserMapper {

    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"callBack", "version", "fullName", "reference", "creator",
        "password", "enabled", "accountExpired", "accountLocked", "passwordExpired", "publicKey", "privateKey"

    })
    UserResponse map(User user);

}
