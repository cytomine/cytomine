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
import be.cytomine.common.repository.model.command.request.CreateTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.CreateUploadedFileCommand;
import be.cytomine.common.repository.model.command.request.DeleteOntologyCommand;
import be.cytomine.common.repository.model.command.request.DeleteStorageCommand;
import be.cytomine.common.repository.model.command.request.DeleteTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteUploadedFileCommand;
import be.cytomine.common.repository.model.command.request.UndoCreateCommand;
import be.cytomine.common.repository.model.command.request.UndoDeleteCommand;
import be.cytomine.common.repository.model.command.request.UndoUpdateCommand;
import be.cytomine.common.repository.model.command.request.UpdateOntologyCommand;
import be.cytomine.common.repository.model.command.request.UpdateStorageCommand;
import be.cytomine.common.repository.model.command.request.UpdateTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateUploadedFileCommand;
import be.cytomine.common.repository.model.storage.payload.CreateStorage;

@Component
@AllArgsConstructor
public class ApplyCommandService {
    private final CommandV2Repository commandRepository;
    private final StorageCommandService storageCommandService;
    private final TagDomainAssociationCommandService tagDomainAssociationCommandService;
    private final TermCommandService termCommandService;
    private final TermRelationCommandService termRelationCommandService;
    private final OntologyCommandService ontologyCommandService;
    private final UploadedFileCommandService uploadedFileCommandService;

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
            case CreateUploadedFileCommand cufc ->
                uploadedFileCommandService.undoCreate(commandEntity.getId(), cufc, userId, now);
            case UpdateUploadedFileCommand uufc ->
                uploadedFileCommandService.undoUpdate(commandEntity.getId(), uufc, userId, now);
            case DeleteUploadedFileCommand dufc ->
                uploadedFileCommandService.undoDelete(commandEntity.getId(), dufc, userId, now);
            case CreateTagDomainAssociationCommand ctdac ->
                tagDomainAssociationCommandService.undoCreate(commandEntity.getId(), ctdac, userId, now);
            case UpdateTagDomainAssociationCommand utdac ->
                tagDomainAssociationCommandService.undoUpdate(commandEntity.getId(), utdac, userId, now);
            case DeleteTagDomainAssociationCommand dtdac ->
                tagDomainAssociationCommandService.undoDelete(commandEntity.getId(), dtdac, userId, now);

            // Actually we undo an undo target here
            case UndoCreateCommand<?> v -> switch (v.target()) {
                case CreateTermCommand ctc -> termCommandService.delete(userId, ctc.id(), now);
                case CreateOntologyCommand coc -> ontologyCommandService.undoCreate(v.commandId(), coc, userId, now);
                default -> null;
            };
            case UndoDeleteCommand<?> v -> switch (v.target()) {
                case DeleteStorageCommand dsc ->
                    storageCommandService.create(userId, new CreateStorage(dsc.before().name()), now);
                case DeleteTermCommand dtc -> termCommandService.undoDelete(v.commandId(), dtc, userId, now);
                case DeleteOntologyCommand doc -> ontologyCommandService.undoDelete(v.commandId(), doc, userId, now);
                default -> null;
            };
            case UndoUpdateCommand<?> v -> switch (v.target()) {
                case UpdateTermCommand updateTermCommand ->
                    termCommandService.updateWithExistingCommand(userId, v.commandId(), updateTermCommand.getCommand(),
                        updateTermCommand.after(), now);
                case UpdateOntologyCommand uoc ->
                    ontologyCommandService.updateWithExistingCommand(userId, v.commandId(), uoc.getCommand(),
                        uoc.after(), now);
                default -> null;
            };
        });
    }
}
