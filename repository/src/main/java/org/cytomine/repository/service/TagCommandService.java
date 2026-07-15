package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TagRepository;
import org.cytomine.repository.persistence.entity.TagEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.TagCommandPayload;
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

    private final CommandV2Repository commandV2Repository;
    private final CommandMapper commandMapper;
    private final TagRepository tagRepository;

    @Override
    public TagEntity updateEntityWithEntity(TagEntity entity, UpdateTag payload, Timestamp now) {
        return null;
    }

    @Override
    public TagEntity updateEntityWithPayload(TagEntity entity, TagCommandPayload payload, Timestamp now) {
        return null;
    }

    @Override
    public TagResponse mapToResponse(TagEntity entity) {
        return null;
    }

    @Override
    public TagEntity mapCreateToEntity(CreateTag createPayload, long userId, Timestamp creationDate) {
        return null;
    }

    @Override
    public TagCommandPayload map(TagEntity entity) {
        return null;
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
    public boolean canWriteId(long userId, long id) {
        return false;
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return false;
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return false;
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return false;
    }
}
