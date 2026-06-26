package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.mapper.UploadedFileMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.UploadedFileRepository;
import org.cytomine.repository.persistence.entity.UploadedFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.request.UploadedFileCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.CreateUploadedFileCommand;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteUploadedFileCommand;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateUploadedFileCommand;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;
import be.cytomine.common.repository.model.uploadedfile.payload.UpdateUploadedFile;

@Component
@RequiredArgsConstructor
@Getter
public class UploadedFileCommandService
    implements CRUDCommandService<
    CreateUploadedFile,
    UpdateUploadedFile,
    UploadedFileCommandPayload,
    UploadedFileEntity,
    UploadedFileResponse> {

    private final ACLService aclService;
    private final CommandMapper commandMapper;
    private final CommandV2Repository commandV2Repository;
    private final UploadedFileMapper uploadedFileMapper;
    private final UploadedFileRepository uploadedFileRepository;
    @Setter
     private  ApplyCommandService applyCommandService;

    @Override
    public UploadedFileEntity save(UploadedFileEntity entity) {
        return uploadedFileRepository.save(entity);
    }

    @Override
    public Optional<UploadedFileEntity> get(long id) {
        return uploadedFileRepository.findById(id);
    }

    @Override
    public UploadedFileEntity mapCreateToEntity(CreateUploadedFile createPayload, long userId, Timestamp creationDate) {
        return uploadedFileMapper.mapToUploadedFileEntity(createPayload, creationDate);
    }

    @Override
    public UploadedFileCommandPayload map(UploadedFileEntity entity) {
        return uploadedFileMapper.mapToUploadedFileCommandPayload(entity);
    }

    @Override
    public UploadedFileResponse mapToResponse(UploadedFileEntity entity) {
        return uploadedFileMapper.mapToUploadedFileResponse(entity);
    }

    @Override
    public UploadedFileEntity updateEntityWithEntity(
        UploadedFileEntity entity,
        UpdateUploadedFile payload,
        Timestamp now
    ) {
        payload.filename().ifPresent(entity::setFilename);
        payload.originalFilename().ifPresent(entity::setOriginalFilename);
        payload.ext().ifPresent(entity::setExt);
        payload.contentType().ifPresent(entity::setContentType);
        payload.size().ifPresent(entity::setSize);
        payload.status().ifPresent(entity::setStatus);
        payload.projects().ifPresent(set -> entity.setProjects(set.toArray(Long[]::new)));
        entity.setUpdated(now);
        return entity;
    }

    @Override
    public UploadedFileEntity updateEntityWithPayload(
        UploadedFileEntity entity,
        UploadedFileCommandPayload payload,
        Timestamp now
    ) {
        return uploadedFileMapper.updateWithPayload(entity, payload, now);
    }

    @Override
    public CreateCommandRequest<UploadedFileCommandPayload> mapCreateCommand(
        long userId,
        UploadedFileCommandPayload after
    ) {
        return new CreateUploadedFileCommand(after, userId);
    }

    @Override
    public UpdateCommandRequest<UploadedFileCommandPayload> mapUpdateCommand(
        long userId,
        UploadedFileCommandPayload before,
        UploadedFileCommandPayload after
    ) {
        return new UpdateUploadedFileCommand(before, after, userId);
    }

    @Override
    public DeleteCommandRequest<UploadedFileCommandPayload> mapDeleteCommand(
        long userId,
        UploadedFileCommandPayload before
    ) {
        return new DeleteUploadedFileCommand(before, userId);
    }

    @Override
    public boolean canWriteId(long userId, long id) {
        return get(id).map(entity -> aclService.canWriteStorage(userId, entity.getStorageId())).orElse(false);
    }

    @Override
    public boolean canDeleteId(long userId, long id) {
        return get(id).map(entity -> aclService.canDeleteStorage(userId, entity.getStorageId())).orElse(false);
    }

    @Override
    public boolean canWriteAclId(long userId, long aclId) {
        return aclService.canWriteStorage(userId, aclId);
    }

    @Override
    public boolean canDeleteAclId(long userId, long aclId) {
        return aclService.canDeleteStorage(userId, aclId);
    }

    public Page<UploadedFileResponse> getAll(long userId, Pageable pageable) {
        List<Long> storageIds = aclService.getAccessibleStorageIds(userId);
        return uploadedFileRepository.search(storageIds, pageable)
            .map(uploadedFileMapper::mapToUploadedFileResponse);
    }
}
