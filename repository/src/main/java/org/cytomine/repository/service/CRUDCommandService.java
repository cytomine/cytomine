package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.cytomine.repository.mapper.CommandMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;

public interface CRUDCommandService<C, U, P extends HasLongId & HasAclId, E,R extends ApplyCommandResponse> {

    E save(E entity);

    P map(E entity);
    R mapToResponse(E entity);

    CommandV2Repository getCommandV2Repository();

    CommandMapper getCommandMapper();

    UpdateCommandRequest<?> mapUpdateCommand(long userId, long id, P before, P after);

    Optional<E> get(long userId, long id);

    Optional<HttpCommandResponse> create(long userId, C createPayload, LocalDateTime now);

    Optional<HttpCommandResponse> delete(long userId, long id, LocalDateTime now);

    default Optional<HttpCommandResponse> update(long userId, long id, U updatePayload, LocalDateTime now) {
        if (canWrite(userId, id)) {

            return get(userId, id).map(e -> {
                P beforePayload = map(e);
                E update = update(e, updatePayload);
                E savedEntity = save(update);
                UpdateCommandRequest<?> updateCommandRequest = mapUpdateCommand(userId, id, beforePayload,
                    map(savedEntity));
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

    Optional<HttpCommandResponse> updateWithExistingCommand(long userId, UUID commandId, P payload, LocalDateTime now);

    Optional<HttpCommandResponse> logicalDelete(UUID commandId, long id, String command, LocalDateTime now);

    Optional<HttpCommandResponse> restore(UUID commandId, long id, String command, LocalDateTime now);

    E update(E entity, U updatePayload);

    boolean canWrite(long userId, long id);

    boolean canDelete(long userId, long id);


    default Optional<HttpCommandResponse> undoDelete(UUID commandId, DeleteCommandRequest<P> deleteCommand, long userId,
                                                     LocalDateTime now) {
        if (!canDelete(userId, deleteCommand.aclId())) {
            return Optional.empty();
        }
        return restore(commandId, deleteCommand.id(), deleteCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> redoDelete(UUID commandId, DeleteCommandRequest<P> deleteCommand, long userId,
                                                     LocalDateTime now) {
        if (!canDelete(userId, deleteCommand.aclId())) {
            return Optional.empty();
        }
        return logicalDelete(commandId, deleteCommand.id(), deleteCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> undoCreate(UUID commandId, CreateCommandRequest<P> createCommand, long userId,
                                                     LocalDateTime now) {
        if (!canWrite(userId, createCommand.aclId())) {
            return Optional.empty();
        }
        return logicalDelete(commandId, createCommand.id(), createCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> redoCreate(UUID commandId, CreateCommandRequest<P> createCommand, long userId,
                                                     LocalDateTime now) {
        if (!canWrite(userId, createCommand.aclId())) {
            return Optional.empty();
        }
        return restore(commandId, createCommand.id(), createCommand.getCommand(), now);
    }

    default Optional<HttpCommandResponse> undoUpdate(UUID commandId, UpdateCommandRequest<P> updateCommand, long userId,
                                                     LocalDateTime now) {
        if (!canWrite(userId, updateCommand.aclId())) {
            return Optional.empty();
        }
        return updateWithExistingCommand(userId, commandId, updateCommand.before(), now);
    }

    default Optional<HttpCommandResponse> redoUpdate(UUID commandId, UpdateCommandRequest<P> updateCommand, long userId,
                                                     LocalDateTime now) {
        if (!canWrite(userId, updateCommand.aclId())) {
            return Optional.empty();
        }
        return updateWithExistingCommand(userId, commandId, updateCommand.after(), now);
    }

}
