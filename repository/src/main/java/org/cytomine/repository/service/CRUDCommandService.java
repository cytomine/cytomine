package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.HasTimestampCUD;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;

public interface CRUDCommandService<C, U, P extends HasLongId & HasAclId, E extends HasTimestampCUD,
    R extends ApplyCommandResponse> {
    E updateEntityWithEntity(E entity, U payload, Timestamp now);

    E updateEntityWithPayload(E entity, P payload, Timestamp now);

    R mapToResponse(E entity);

    E mapCreateToEntity(C createPayload, long userId, Timestamp creationDate);

    P map(E entity);

    E save(E entity);

    CommandV2Repository getCommandV2Repository();

    CommandMapper getCommandMapper();

    UpdateCommandRequest<P> mapUpdateCommand(long userId, P before, P after);

    CreateCommandRequest<P> mapCreateCommand(long userId, P after);

    DeleteCommandRequest<P> mapDeleteCommand(long userId, P before);

    Optional<E> get(long id);

    default Optional<HttpCommandResponse> delete(long userId, long id, Instant now) {
        if (canDeleteId(userId, id)) {
            return get(id).map(entity -> {
                DeleteCommandRequest<P> deleteCommandRequest = mapDeleteCommand(userId, map(entity));
                CommandV2Entity commandV2Entity = getCommandV2Repository().save(getCommandMapper().map(
                    deleteCommandRequest,
                    Timestamp.from(now),
                    Timestamp.from(now),
                    userId
                ));
                entity.setDeleted(Timestamp.from(now));
                E savedEntity = save(entity);
                R response = mapToResponse(savedEntity);
                return new HttpCommandResponse(
                    true,
                    response,
                    commandV2Entity.getId(),
                    deleteCommandRequest.getCommand()
                );
            });
        } else {
            return Optional.empty();
        }
    }

    default Optional<HttpCommandResponse> update(long userId, long id, U updatePayload, Instant now) {
        if (canWriteId(userId, id)) {
            return get(id).map(e -> {
                P beforePayload = map(e);
                E update = updateEntityWithEntity(e, updatePayload, Timestamp.from(now));
                E savedEntity = save(update);
                UpdateCommandRequest<?> updateCommandRequest = mapUpdateCommand(
                    userId,
                    beforePayload,
                    map(savedEntity)
                );
                CommandV2Entity commandV2Entity = getCommandV2Repository().save(getCommandMapper().map(
                    updateCommandRequest,
                    Timestamp.from(now),
                    Timestamp.from(now),
                    userId
                ));
                R response = mapToResponse(savedEntity);
                return new HttpCommandResponse(
                    true,
                    response,
                    commandV2Entity.getId(),
                    updateCommandRequest.getCommand()
                );
            });
        } else {
            return Optional.empty();
        }
    }

    default Optional<HttpCommandResponse> create(long userId, C createPayload, Instant now) {
        E entity = mapCreateToEntity(createPayload, userId, Timestamp.from(now));
        E savedEntity = save(entity);
        P commandPayload = map(savedEntity);
        CreateCommandRequest<?> createCommandRequest = mapCreateCommand(userId, commandPayload);
        CommandV2Entity commandV2Entity = getCommandV2Repository().save(getCommandMapper().map(
            createCommandRequest,
            Timestamp.from(now),
            null,
            userId
        ));
        R response = mapToResponse(savedEntity);
        return Optional.of(new HttpCommandResponse(
            true,
            response,
            commandV2Entity.getId(),
            createCommandRequest.getCommand()
        ));
    }

    default Optional<HttpCommandResponse> logicalDelete(UUID commandId, long id, String command, Instant now) {
        return get(id).map(entity -> {
            entity.setDeleted(Timestamp.from(now));
            return saveAndBuildResponse(entity, command, commandId);
        });
    }

    default Optional<HttpCommandResponse> restore(
        UUID commandId,
        long userId,
        long id,
        long aclId,
        String command,
        Instant now
    ) {
        if (canWriteAclId(userId, aclId)) {
            return get(id).map(entity -> {
                entity.setDeleted(null);
                entity.setUpdated(Timestamp.from(now));
                return saveAndBuildResponse(entity, command, commandId);
            });
        } else {
            return Optional.empty();
        }
    }

    default HttpCommandResponse saveAndBuildResponse(E entity, String command, UUID commandId) {
        E saved = save(entity);
        R response = mapToResponse(saved);
        return new HttpCommandResponse(true, response, commandId, command);
    }

    default Optional<HttpCommandResponse> updateWithExistingCommand(
        long userId,
        UUID commandId,
        String command,
        P payload,
        Instant now
    ) {
        return get(payload.id()).map(entity -> {
            E updatedEntity = updateEntityWithPayload(entity, payload, Timestamp.from(now));
            return saveAndBuildResponse(updatedEntity, command, commandId);
        });
    }

    boolean canWriteId(long userId, long id);

    boolean canDeleteId(long userId, long id);

    boolean canWriteAclId(long userId, long id);

    boolean canDeleteAclId(long userId, long id);

    default Optional<HttpCommandResponse> undoDelete(
        UUID commandId,
        DeleteCommandRequest<P> deleteCommand,
        long userId,
        Instant now
    ) {
        if (!canWriteAclId(userId, deleteCommand.aclId())) {
            return Optional.empty();
        }
        return restore(commandId, userId, deleteCommand.id(), deleteCommand.aclId(), deleteCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> redoDelete(
        UUID commandId,
        DeleteCommandRequest<P> deleteCommand,
        long userId,
        Instant now
    ) {
        if (!canDeleteAclId(userId, deleteCommand.aclId())) {
            return Optional.empty();
        }
        return logicalDelete(commandId, deleteCommand.id(), deleteCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> undoCreate(
        UUID commandId,
        CreateCommandRequest<P> createCommand,
        long userId,
        Instant now
    ) {
        if (!canWriteAclId(userId, createCommand.aclId())) {
            return Optional.empty();
        }
        return logicalDelete(commandId, createCommand.id(), createCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> redoCreate(
        UUID commandId,
        CreateCommandRequest<P> createCommand,
        long userId,
        Instant now
    ) {
        if (!canWriteAclId(userId, createCommand.aclId())) {
            return Optional.empty();
        }
        return restore(commandId, userId, createCommand.id(), createCommand.aclId(), createCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> undoUpdate(
        UUID commandId,
        UpdateCommandRequest<P> updateCommand,
        long userId,
        Instant now
    ) {
        if (!canWriteAclId(userId, updateCommand.aclId())) {
            return Optional.empty();
        }
        return updateWithExistingCommand(userId, commandId, updateCommand.getCommand(), updateCommand.before(), now);
    }

    default Optional<HttpCommandResponse> redoUpdate(
        UUID commandId,
        UpdateCommandRequest<P> updateCommand,
        long userId,
        Instant now
    ) {
        if (!canWriteAclId(userId, updateCommand.aclId())) {
            return Optional.empty();
        }
        return updateWithExistingCommand(userId, commandId, updateCommand.getCommand(), updateCommand.after(), now);
    }
}
