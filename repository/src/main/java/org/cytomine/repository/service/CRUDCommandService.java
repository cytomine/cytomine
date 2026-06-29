package org.cytomine.repository.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
import be.cytomine.common.repository.model.command.payload.response.UndoCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.UndoCommandRequest;
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

    default Optional<HttpCommandResponse> delete(long userId, long id, LocalDateTime now) {
        if (canDeleteId(userId, id)) {
            return get(id).map(entity -> {
                DeleteCommandRequest<P> deleteCommandRequest = mapDeleteCommand(userId, map(entity));
                CommandV2Entity commandV2Entity =
                    getCommandV2Repository().save(getCommandMapper().map(deleteCommandRequest, now, now, userId));
                entity.setDeleted(Timestamp.valueOf(now));
                E savedEntity = save(entity);
                R response = mapToResponse(savedEntity);
                return new HttpCommandResponse(true, response, commandV2Entity.getId(),
                    deleteCommandRequest.getCommand());
            });
        } else {
            return Optional.empty();
        }

    }

    default Optional<HttpCommandResponse> update(long userId, long id, U updatePayload, LocalDateTime now) {
        if (canWriteId(userId, id)) {
            return get(id).map(e -> {
                P beforePayload = map(e);
                E update = updateEntityWithEntity(e, updatePayload, Timestamp.valueOf(now));
                E savedEntity = save(update);
                UpdateCommandRequest<?> updateCommandRequest =
                    mapUpdateCommand(userId, beforePayload, map(savedEntity));
                CommandV2Entity commandV2Entity =
                    getCommandV2Repository().save(getCommandMapper().map(updateCommandRequest, now, now, userId));
                R response = mapToResponse(savedEntity);
                return new HttpCommandResponse(true, response, commandV2Entity.getId(),
                    updateCommandRequest.getCommand());
            });
        } else {
            return Optional.empty();
        }
    }

    default Optional<HttpCommandResponse> create(long userId, C createPayload, LocalDateTime now) {
        E entity = mapCreateToEntity(createPayload, userId, Timestamp.valueOf(now));
        E savedEntity = save(entity);
        P commandPayload = map(savedEntity);
        CreateCommandRequest<?> createCommandRequest = mapCreateCommand(userId, commandPayload);
        CommandV2Entity commandV2Entity =
            getCommandV2Repository().save(getCommandMapper().map(createCommandRequest, now, null, userId));
        R response = mapToResponse(savedEntity);
        return Optional.of(
            new HttpCommandResponse(true, response, commandV2Entity.getId(), createCommandRequest.getCommand()));
    }

    default Optional<HttpCommandResponse> updateWithExistingCommand(long userId, UUID commandId, String command,
        P payload, LocalDateTime now) {
        return get(payload.id()).map(entity -> {
            E updatedEntity = updateEntityWithPayload(entity, payload, Timestamp.valueOf(now));
            E saved = save(updatedEntity);
            R response = mapToResponse(saved);
            return new HttpCommandResponse(true, new UndoCommandResponse(response), commandId, command);
        });
    }

    boolean canWriteId(long userId, long id);

    boolean canDeleteId(long userId, long id);

    boolean canWriteAclId(long userId, long id);

    boolean canDeleteAclId(long userId, long id);

    default Optional<HttpCommandResponse> undoDelete(UUID commandId, DeleteCommandRequest<P> deleteCommand, long userId,
        LocalDateTime now) {
        if (!canWriteAclId(userId, deleteCommand.aclId())) {
            return Optional.empty();
        }

        UndoCommandRequest<P> undoCommandRequest = new UndoCommandRequest<>(deleteCommand, commandId);
        CommandV2Entity commandV2Entity =
            getCommandV2Repository().save(getCommandMapper().map(undoCommandRequest, now, now, userId));

        long id = deleteCommand.id();
        long aclId = deleteCommand.aclId();
        String command = deleteCommand.getCommand();
        if (canWriteAclId(userId, aclId)) {
            return get(id).map(entity -> {
                entity.setDeleted(null);
                entity.setUpdated(Timestamp.valueOf(now));
                E saved = save(entity);
                R response = mapToResponse(saved);
                return new HttpCommandResponse(true, new UndoCommandResponse(response), commandV2Entity.getId(),
                    command);
            });
        } else {
            return Optional.empty();
        }
    }

    default Optional<HttpCommandResponse> undoCreate(UUID commandId, CreateCommandRequest<P> createCommand, long userId,
        LocalDateTime now) {
        if (!canDeleteAclId(userId, createCommand.aclId())) {
            return Optional.empty();
        }

        UndoCommandRequest<P> undoCommandRequest = new UndoCommandRequest<>(createCommand, commandId);
        CommandV2Entity commandV2Entity =
            getCommandV2Repository().save(getCommandMapper().map(undoCommandRequest, now, now, userId));

        long id = createCommand.id();
        String command = createCommand.getCommand();
        return get(id).map(entity -> {
            entity.setDeleted(Timestamp.valueOf(now));
            E saved = save(entity);
            R response = mapToResponse(saved);
            return new HttpCommandResponse(true, new UndoCommandResponse(response), commandV2Entity.getId(), command);
        });
    }

    default Optional<HttpCommandResponse> undoUpdate(UUID commandId, UpdateCommandRequest<P> updateCommand, long userId,
        LocalDateTime now) {
        if (!canWriteAclId(userId, updateCommand.aclId())) {
            return Optional.empty();
        }

        UndoCommandRequest<P> undoCommandRequest = new UndoCommandRequest<>(updateCommand, commandId);
        CommandV2Entity commandV2Entity =
            getCommandV2Repository().save(getCommandMapper().map(undoCommandRequest, now, now, userId));

        return updateWithExistingCommand(userId, commandV2Entity.getId(), updateCommand.getCommand(),
            updateCommand.before(), now);
    }
}
