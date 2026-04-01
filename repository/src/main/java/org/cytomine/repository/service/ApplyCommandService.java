package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.CreateTermCommand;
import be.cytomine.common.repository.model.command.DeleteTermCommand;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.UpdateTermCommand;

@Component
@AllArgsConstructor
public class ApplyCommandService {
    private final CommandV2Repository commandRepository;
    private final ACLService aclService;
    private final TermCommandService termCommandService;
    private final OntologyMapper ontologyMapper;

    @Transactional
    public Optional<HttpCommandResponse<TermResponse>> undoCommand(long userId, UUID undoCommand) {
        LocalDateTime now = LocalDateTime.now();
        return commandRepository.findById(undoCommand)
                   .filter(commandEntity -> userCanUndoCommand(userId, commandEntity))
                   .flatMap(commandEntity -> switch (commandEntity.getData()) {
                       case DeleteTermCommand dtc ->
                           termCommandService.undoDeleteTerm(commandEntity.getId(), dtc, userId,
                               now);
                       case CreateTermCommand icr -> termCommandService.undoCreateTerm(commandEntity.getId(), icr,
                           userId,
                           now);
                       case UpdateTermCommand ucr -> termCommandService.undoUpdateTerm(commandEntity.getId(), ucr,
                           userId,
                           now);
                   });
    }

    boolean userCanUndoCommand(long userId, CommandV2Entity commandEntity) {
        return switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> aclService.canWriteOntology(userId, dtc.ontologyId());
            case CreateTermCommand icr -> aclService.canWriteOntology(userId, icr.ontologyId());
            case UpdateTermCommand ucr -> aclService.canWriteOntology(userId, ucr.ontologyId());
        };
    }

    public Optional<Long> redoCommand(long userId, UUID undoCommand) {
        return commandRepository.findById(undoCommand)
                   .filter(commandEntity -> userCanUndoCommand(userId, commandEntity))
                   .flatMap(commandEntity -> switch (commandEntity.getData()) {
                       case DeleteTermCommand dtc -> null;
                       case CreateTermCommand icr -> null;
                       case UpdateTermCommand ucr -> null;
                   });
    }



}
