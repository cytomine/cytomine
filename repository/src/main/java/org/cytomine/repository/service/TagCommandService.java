package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.TagMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TagDomainAssociationRepository;
import org.cytomine.repository.persistence.TagRepository;
import org.cytomine.repository.persistence.UserRepository;
import org.cytomine.repository.persistence.entity.TagEntity;
import org.cytomine.repository.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.TagCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateTagCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteTagCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateTagCommand;
import be.cytomine.common.repository.model.tag.payload.CreateTag;
import be.cytomine.common.repository.model.tag.payload.UpdateTag;

@Component
@RequiredArgsConstructor
@Getter
public class TagCommandService
    implements CRUDCommandService<CreateTag, UpdateTag, TagCommandPayload, TagEntity, TagResponse> {
    private final ACLService aclService;
    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final TagMapper tagMapper;
    private final TagRepository tagRepository;
    private final TagDomainAssociationCommandService tagDomainAssociationCommandService;
    private final TagDomainAssociationRepository tagDomainAssociationRepository;
    private final UserRepository userRepository;
    @Setter
    private ApplyCommandService applyCommandService;

    @Override
    public TagEntity updateEntityWithEntity(TagEntity entity, UpdateTag payload, Timestamp now) {
        return tagMapper.update(entity, payload.name().orElse(entity.getName()), now);
    }

    @Override
    public TagEntity updateEntityWithPayload(TagEntity entity, TagCommandPayload payload, Timestamp now) {
        return tagMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public TagResponse mapToResponse(TagEntity entity) {
        String creatorName = userRepository.findById(entity.getUserId())
            .map(UserEntity::getUsername)
            .orElse(null);
        return tagMapper.mapToTagResponse(entity, creatorName);
    }

    @Override
    public TagEntity mapCreateToEntity(CreateTag createPayload, long userId, Timestamp creationDate) {
        return tagMapper.mapToTagEntity(createPayload, userId, creationDate);
    }

    @Override
    public TagCommandPayload map(TagEntity entity) {
        return tagMapper.mapToCommandPayload(entity);
    }

    @Override
    public TagEntity save(TagEntity entity) {
        return tagRepository.save(entity);
    }

    @Override
    public UpdateCommandRequest<TagCommandPayload> mapUpdateCommand(
        long userId,
        TagCommandPayload before,
        TagCommandPayload after
    ) {
        return new UpdateTagCommand(before, after, userId);
    }

    @Override
    public CreateCommandRequest<TagCommandPayload> mapCreateCommand(long userId, TagCommandPayload after) {
        return new CreateTagCommand(after, userId);
    }

    @Override
    public DeleteCommandRequest<TagCommandPayload> mapDeleteCommand(long userId, TagCommandPayload before) {
        return new DeleteTagCommand(before, userId);
    }

    @Override
    public Optional<TagEntity> get(long id) {
        return tagRepository.findById(id);
    }

    @Override
    public Set<HttpCommandResponse> deleteSubEntities(long userId, long id, LocalDateTime now, UUID parentCommandId) {
        return tagDomainAssociationCommandService.deleteByTagId(userId, id, now, parentCommandId);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        if (aclService.isAdmin(userId)) {
            return true;
        }
        return isCreator(userId, id) && !tagDomainAssociationRepository.existsByTagIdAndDeletedNull(id);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return aclService.isAdmin(userId) || isCreator(userId, id);
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return canWriteId(userId, id);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return canDeleteId(userId, id);
    }

    private boolean isCreator(long userId, long tagId) {
        return tagRepository.findById(tagId)
            .map(tag -> Objects.equals(tag.getUserId(), userId))
            .orElse(false);
    }
}
