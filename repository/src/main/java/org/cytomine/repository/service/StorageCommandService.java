package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.StorageMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.StorageRepository;
import org.cytomine.repository.persistence.entity.StorageEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.StorageCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.StorageResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateStorageCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteStorageCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateStorageCommand;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;
import be.cytomine.common.repository.model.storage.payload.UpdateStorage;

@Component
@RequiredArgsConstructor
@Getter
public class StorageCommandService
    implements CRUDCommandService<CreateStorage, UpdateStorage, StorageCommandPayload, StorageEntity, StorageResponse> {

    private final ACLService aclService;
    private final CommandMapper commandMapper;
    private final CommandV2Repository commandV2Repository;
    private final ApplyCommandService applyCommandService;
    private final StorageMapper storageMapper;
    private final StorageRepository storageRepository;

    @Override
    public StorageEntity save(StorageEntity entity) {
        return storageRepository.save(entity);
    }

    @Override
    public Optional<StorageEntity> get(long id) {
        return storageRepository.findById(id);
    }

    @Override
    public StorageEntity mapCreateToEntity(CreateStorage createPayload, long userId, Timestamp creationDate) {
        return storageMapper.mapToStorageEntity(createPayload, userId, creationDate);
    }

    @Override
    public StorageCommandPayload map(StorageEntity entity) {
        return storageMapper.mapToStorageCommandPayload(entity);
    }

    @Override
    public StorageResponse mapToResponse(StorageEntity entity) {
        return storageMapper.mapToStorageResponse(entity);
    }

    @Override
    public StorageEntity updateEntityWithEntity(StorageEntity entity, UpdateStorage payload, Timestamp now) {
        return storageMapper.update(entity, payload.name().orElse(entity.getName()), now);
    }

    @Override
    public StorageEntity updateEntityWithPayload(StorageEntity entity, StorageCommandPayload payload, Timestamp now) {
        return storageMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public CreateCommandRequest<StorageCommandPayload> mapCreateCommand(long userId, StorageCommandPayload after) {
        return new CreateStorageCommand(after, userId);
    }

    @Override
    public UpdateCommandRequest<StorageCommandPayload> mapUpdateCommand(
        long userId,
        StorageCommandPayload before,
        StorageCommandPayload after
    ) {
        return new UpdateStorageCommand(before, after, userId);
    }

    @Override
    public DeleteCommandRequest<StorageCommandPayload> mapDeleteCommand(long userId, StorageCommandPayload before) {
        return new DeleteStorageCommand(before, userId);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return aclService.canWriteStorage(userId, id);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return aclService.canDeleteStorage(userId, id);
    }

    @Override
    public boolean canWriteAclId(long userId, long id) {
        return aclService.canWriteStorage(userId, id);
    }

    @Override
    public boolean canDeleteAclId(long userId, long id) {
        return aclService.canDeleteStorage(userId, id);
    }
}
