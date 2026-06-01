package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateCommandRequest;
import be.cytomine.common.repository.model.command.request.DeleteCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateCommandRequest;

public interface CRUDCommandService<C, U, P extends HasLongId & HasAclId> {

    Optional<HttpCommandResponse> create(long userId, C createPayload, LocalDateTime now);

    Optional<HttpCommandResponse> delete(long userId, long id, LocalDateTime now);

    Optional<HttpCommandResponse> update(long userId, long id, U updatePayload, LocalDateTime now);

    Optional<HttpCommandResponse> updateWithExistingCommand(long userId, UUID commandId, P payload, LocalDateTime now);

    Optional<HttpCommandResponse> logicalDelete(UUID commandId, long id, String command, LocalDateTime now);

    Optional<HttpCommandResponse> restore(UUID commandId, long id, String command, LocalDateTime now);

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
