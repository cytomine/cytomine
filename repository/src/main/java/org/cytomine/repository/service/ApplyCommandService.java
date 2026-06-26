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
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.CreateTermRelationCommand;
import be.cytomine.common.repository.model.command.request.DeleteOntologyCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateOntologyCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermRelationCommand;

@Component
@AllArgsConstructor
public class ApplyCommandService {
    private final CommandV2Repository commandRepository;
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
        });
    }

    public Optional<HttpCommandResponse> redoCommand(long userId, UUID redoCommand, LocalDateTime now) {
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
        });
    }


}
