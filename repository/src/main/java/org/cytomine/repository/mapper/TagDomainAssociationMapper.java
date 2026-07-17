package org.cytomine.repository.mapper;

import java.sql.Timestamp;

import org.cytomine.repository.persistence.entity.TagDomainAssociationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import be.cytomine.common.repository.model.command.payload.request.TagDomainAssociationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.tagdomainassociation.payload.CreateTagDomainAssociation;
import be.cytomine.common.mapper.BaseMapper;

@Mapper(componentModel = "spring", uses = BaseMapper.class)
public interface TagDomainAssociationMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    TagDomainAssociationResponse mapToResponse(TagDomainAssociationEntity entity);

    @BeanMapping(ignoreUnmappedSourceProperties = {"version"})
    TagDomainAssociationCommandPayload mapToCommandPayload(TagDomainAssociationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", source = "creationDate")
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    TagDomainAssociationEntity mapToEntity(CreateTagDomainAssociation payload, Timestamp creationDate);

    @Mapping(target = "tagId", source = "newTagId")
    @Mapping(target = "domainClassName", source = "newDomainClassName")
    @Mapping(target = "domainId", source = "newDomainId")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"tagId", "domainClassName", "domainId", "updated"})
    TagDomainAssociationEntity update(
        TagDomainAssociationEntity entity,
        long newTagId,
        String newDomainClassName,
        long newDomainId,
        Timestamp now
    );

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "tagId", source = "payload.tagId")
    @Mapping(target = "domainClassName", source = "payload.domainClassName")
    @Mapping(target = "domainId", source = "payload.domainId")
    @Mapping(target = "created", source = "payload.created")
    @Mapping(target = "deleted", source = "payload.deleted")
    @Mapping(target = "updated", source = "now")
    @BeanMapping(ignoreUnmappedSourceProperties = {"tagId", "domainClassName", "domainId", "updated"})
    TagDomainAssociationEntity updateWithPayload(
        TagDomainAssociationEntity entity,
        TagDomainAssociationCommandPayload payload,
        Timestamp now
    );
}
