package org.cytomine.repository.mapper;

import java.sql.Timestamp;
import java.util.Set;

import org.cytomine.repository.persistence.entity.RoleEntity;
import org.cytomine.repository.persistence.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.UserCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.common.repository.model.user.payload.CreateUser;
import be.cytomine.common.mapper.BaseMapper;
import be.cytomine.common.repository.utils.Language;

@Mapper(componentModel = "spring", uses = {BaseMapper.class, RoleMapper.class})
public interface UserMapper {

    default Language mapToLanguage(String language) {
        if (language.length() > 2) {
            return Language.valueOf(language);
        } else {
            return Language.findByCode(language);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "now")
    @Mapping(target = "developer", source = "entity.developer")
    @BeanMapping(ignoreUnmappedSourceProperties = {"role"})
    UserEntity mapToUserEntity(CreateUser entity, long userId, Timestamp now, Set<RoleEntity> roles);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "created", "updated", "deleted"})
    UserCommandPayload mapToUserCommandPayload(UserEntity entity);

    @Mapping(target = "isDeveloper", source = "entity.developer")
    @BeanMapping(ignoreUnmappedSourceProperties = {"version", "created", "updated", "deleted"})
    UserResponse mapToUserResponse(UserEntity entity);

    @Mapping(target = "email", source = "newEmail")
    @Mapping(target = "name", source = "newName")
    @Mapping(target = "firstname", source = "newFirstname")
    @Mapping(target = "lastname", source = "newLastname")
    @Mapping(target = "language", source = "newLanguage")
    @Mapping(target = "privateKey", source = "privateKey")
    @Mapping(target = "publicKey", source = "publicKey")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"username", "email", "name", "firstname", "lastname", "language",
        "updated", "privateKey", "publicKey", "origin"})
    UserEntity update(UserEntity entity, String newEmail, String newName, String newFirstname, String newLastname,
        String newLanguage, String publicKey, String privateKey, String origin, Timestamp now);

    @Mapping(target = "username", source = "replace.username")
    @Mapping(target = "name", source = "replace.name")
    @Mapping(target = "email", source = "replace.email")
    @Mapping(target = "firstname", source = "replace.firstname")
    @Mapping(target = "lastname", source = "replace.lastname")
    @Mapping(target = "language", source = "replace.language")
    @Mapping(target = "developer", source = "replace.developer")
    @Mapping(target = "origin", source = "replace.origin")
    @Mapping(target = "privateKey", source = "replace.privateKey")
    @Mapping(target = "publicKey", source = "replace.publicKey")
    @Mapping(target = "roles", source = "replace.roles")
    @Mapping(target = "updated", source = "now")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "version", source = "entity.version")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "deleted", source = "entity.deleted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"updated"})
    UserEntity updateWithPayload(UserEntity entity, UserCommandPayload replace, Timestamp now);
}
