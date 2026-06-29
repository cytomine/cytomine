package org.cytomine.repository.service;

import java.time.Instant;
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
import be.cytomine.common.repository.model.command.request.UpdateOntologyCommand;
import be.cytomine.common.repository.model.command.request.UpdateStorageCommand;
import be.cytomine.common.repository.model.command.request.UpdateTagDomainAssociationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateUploadedFileCommand;

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
    public Optional<HttpCommandResponse> undoCommand(long userId, UUID undoCommand, Instant now) {
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
        });
    }

    public Optional<HttpCommandResponse> redoCommand(long userId, UUID redoCommand, Instant now) {
        return commandRepository.findById(redoCommand).flatMap(commandEntity -> switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> termCommandService.redoDelete(commandEntity.getId(), dtc, userId, now);
            case CreateTermCommand icr -> termCommandService.redoCreate(commandEntity.getId(), icr, userId, now);
            case UpdateTermCommand ucr -> termCommandService.redoUpdate(commandEntity.getId(), ucr, userId, now);
            case DeleteTermRelationCommand ucr ->
                termRelationCommandService.redoDelete(commandEntity.getId(), ucr, userId, now);
            case CreateTermRelationCommand ctrc ->
                termRelationCommandService.redoCreate(commandEntity.getId(), ctrc, userId, now);
            case UpdateTermRelationCommand utrc ->
                termRelationCommandService.redoUpdate(commandEntity.getId(), utrc, userId, now);
            case CreateOntologyCommand createOntologyCommand ->
                ontologyCommandService.redoCreate(commandEntity.getId(), createOntologyCommand, userId, now);
            case DeleteOntologyCommand deleteOntologyCommand ->
                ontologyCommandService.redoDelete(commandEntity.getId(), deleteOntologyCommand, userId, now);
            case UpdateOntologyCommand updateOntologyCommand ->
                ontologyCommandService.redoUpdate(commandEntity.getId(), updateOntologyCommand, userId, now);
            case CreateStorageCommand csc -> storageCommandService.redoCreate(commandEntity.getId(), csc, userId, now);
            case UpdateStorageCommand usc -> storageCommandService.redoUpdate(commandEntity.getId(), usc, userId, now);
            case DeleteStorageCommand dsc -> storageCommandService.redoDelete(commandEntity.getId(), dsc, userId, now);
            case CreateUploadedFileCommand cufc ->
                uploadedFileCommandService.redoCreate(commandEntity.getId(), cufc, userId, now);
            case UpdateUploadedFileCommand uufc ->
                uploadedFileCommandService.redoUpdate(commandEntity.getId(), uufc, userId, now);
            case DeleteUploadedFileCommand dufc ->
                uploadedFileCommandService.redoDelete(commandEntity.getId(), dufc, userId, now);
            case CreateTagDomainAssociationCommand ctdac ->
                tagDomainAssociationCommandService.redoCreate(commandEntity.getId(), ctdac, userId, now);
            case UpdateTagDomainAssociationCommand utdac ->
                tagDomainAssociationCommandService.redoUpdate(commandEntity.getId(), utdac, userId, now);
            case DeleteTagDomainAssociationCommand dtdac ->
                tagDomainAssociationCommandService.redoDelete(commandEntity.getId(), dtdac, userId, now);
        });
    }
}
