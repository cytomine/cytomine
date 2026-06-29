package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateOntologyCommand;
import be.cytomine.common.repository.model.command.request.CreateStorageCommand;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteOntologyCommand;
import be.cytomine.common.repository.model.command.request.DeleteStorageCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UndoCommandRequest;
import be.cytomine.common.repository.model.command.request.UpdateOntologyCommand;
import be.cytomine.common.repository.model.command.request.UpdateStorageCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;

@Component
@AllArgsConstructor
public class ApplyCommandService {
    private final CommandV2Repository commandRepository;
    private final StorageCommandService storageCommandService;
    private final TermCommandService termCommandService;
    private final TermRelationCommandService termRelationCommandService;
    private final OntologyCommandService ontologyCommandService;

    @Transactional
    public Optional<HttpCommandResponse> undoCommand(long userId, UUID undoCommand, LocalDateTime now) {
        return commandRepository.findById(undoCommand).flatMap(commandEntity -> switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> termCommandService.undoDelete(commandEntity.getId(), dtc, userId, now);
            case CreateTermCommand icr -> termCommandService.undoCreate(commandEntity.getId(), icr, userId, now);
            case UpdateTermCommand ucr -> termCommandService.undoUpdate(commandEntity.getId(), ucr, userId, now);
            case DeleteTermRelationCommand deleteTermRelationCommand ->
                termRelationCommandService.undoDelete(commandEntity.getId(), deleteTermRelationCommand, userId, now);
            case CreateTermRelationCommand ctrc ->
                termRelationCommandService.undoCreate(commandEntity.getId(), ctrc, userId, now);
            case UpdateTermRelationCommand utrc ->
                termRelationCommandService.undoUpdate(commandEntity.getId(), utrc, userId, now);
            case CreateOntologyCommand createOntologyCommand ->
                ontologyCommandService.undoCreate(commandEntity.getId(), createOntologyCommand, userId, now);
            case DeleteOntologyCommand deleteOntologyCommand ->
                ontologyCommandService.undoDelete(commandEntity.getId(), deleteOntologyCommand, userId, now);
            case UpdateOntologyCommand updateOntologyCommand ->
                ontologyCommandService.undoUpdate(commandEntity.getId(), updateOntologyCommand, userId, now);
            case CreateStorageCommand csc -> storageCommandService.undoCreate(commandEntity.getId(), csc, userId, now);
            case UpdateStorageCommand usc -> storageCommandService.undoUpdate(commandEntity.getId(), usc, userId, now);
            case DeleteStorageCommand dsc -> storageCommandService.undoDelete(commandEntity.getId(), dsc, userId, now);

            // Actually we undo an undo command here
            case UndoCommandRequest<?> v -> switch (v.command()) {
                case DeleteStorageCommand dsc ->
                    storageCommandService.create(userId, new CreateStorage(dsc.before().name()), now);
                case CreateTermCommand ctc -> termCommandService.delete(userId, ctc.id(), now);
                case DeleteTermCommand dtc ->
                    termCommandService.restore(v.commandId(), userId, dtc.id(), dtc.aclId(), dtc.getCommand(), now);
                case UpdateTermCommand updateTermCommand ->
                    termCommandService.updateWithExistingCommand(userId, v.commandId(), updateTermCommand.getCommand(),
                        updateTermCommand.after(), now);
                default -> null;
            };
        });
    }
}
