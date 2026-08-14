package be.cytomine.mapper;

import java.util.Optional;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.mapper.BaseMapper;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.utils.Language;
import be.cytomine.domain.security.User;

@Mapper(componentModel = "spring", uses = {BaseMapper.class, RoleMapper.class})
public interface UserMapper {

    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"callBack", "version", "fullName", "reference", "creator",
        "password", "enabled", "accountExpired", "accountLocked", "passwordExpired", "publicKey", "privateKey",
        "deleted"})
    UserResponse map(User user);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "callBack", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountExpired", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "passwordExpired", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"dataType"})
    User map(UserResponse userResponse);

    default Language map(Optional<String> maybeLanguage) {
        return maybeLanguage.map(language -> Enum.valueOf(Language.class, language)).orElse(null);
    }
}
