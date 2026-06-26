package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.TagDomainAssociationMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TagDomainAssociationRepository;
import org.cytomine.repository.persistence.entity.TagDomainAssociationEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.TagDomainAssociationCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateTagDomainAssociationCommand;
import be.cytomine.common.repository.model.tagdomainassociation.payload.CreateTagDomainAssociation;
import be.cytomine.common.repository.model.tagdomainassociation.payload.UpdateTagDomainAssociation;

@Component
@RequiredArgsConstructor
@Getter
public class TagDomainAssociationCommandService implements
    CRUDCommandService<CreateTagDomainAssociation, UpdateTagDomainAssociation, TagDomainAssociationCommandPayload,
        TagDomainAssociationEntity, TagDomainAssociationResponse> {

    private final TagDomainAssociationRepository tagDomainAssociationRepository;
    private final TagDomainAssociationMapper tagDomainAssociationMapper;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final ACLService aclService;

    @Override
    public TagDomainAssociationEntity save(TagDomainAssociationEntity entity) {
        return tagDomainAssociationRepository.save(entity);
    }

    @Override
    public Optional<TagDomainAssociationEntity> get(long id) {
        return tagDomainAssociationRepository.findById(id);
    }

    @Override
    public TagDomainAssociationEntity mapCreateToEntity(
        CreateTagDomainAssociation payload,
        long userId,
        Timestamp creationDate
    ) {
        return tagDomainAssociationMapper.mapToEntity(payload, creationDate);
    }

    @Override
    public TagDomainAssociationCommandPayload map(TagDomainAssociationEntity entity) {
        return tagDomainAssociationMapper.mapToCommandPayload(entity);
    }

    @Override
    public TagDomainAssociationResponse mapToResponse(TagDomainAssociationEntity entity) {
        return tagDomainAssociationMapper.mapToResponse(entity);
    }

    @Override
    public TagDomainAssociationEntity updateEntityWithEntity(
        TagDomainAssociationEntity entity,
        UpdateTagDomainAssociation payload,
        Timestamp now
    ) {
        return tagDomainAssociationMapper.update(
            entity,
            payload.tagId().orElse(entity.getTagId()),
            payload.domainClassName().orElse(entity.getDomainClassName()),
            payload.domainId().orElse(entity.getDomainId()),
            now
        );
    }

    @Override
    public TagDomainAssociationEntity updateEntityWithPayload(
        TagDomainAssociationEntity entity,
        TagDomainAssociationCommandPayload payload,
        Timestamp now
    ) {
        return tagDomainAssociationMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public CreateCommandRequest<TagDomainAssociationCommandPayload> mapCreateCommand(
        long userId,
        TagDomainAssociationCommandPayload after
    ) {
        return new CreateTagDomainAssociationCommand(after, userId);
    }

    @Override
    public UpdateCommandRequest<TagDomainAssociationCommandPayload> mapUpdateCommand(
        long userId,
        TagDomainAssociationCommandPayload before,
        TagDomainAssociationCommandPayload after
    ) {
        return new UpdateTagDomainAssociationCommand(before, after, userId);
    }

    @Override
    public DeleteCommandRequest<TagDomainAssociationCommandPayload> mapDeleteCommand(
        long userId,
        TagDomainAssociationCommandPayload before
    ) {
        return new DeleteTagDomainAssociationCommand(before, userId);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return tagDomainAssociationRepository.findById(id)
            .map(e -> aclService.canWriteDomain(userId, e.getDomainId(), e.getDomainClassName()))
            .orElse(false);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return tagDomainAssociationRepository.findById(id)
            .map(e -> aclService.canDeleteDomain(userId, e.getDomainId(), e.getDomainClassName()))
            .orElse(false);
    }

    @Override
    public boolean canWriteAclId(long userId, long aclId) {
        return aclService.isAdmin(userId);
    }

    @Override
    public boolean canDeleteAclId(long userId, long aclId) {
        return aclService.isAdmin(userId);
    }
}
