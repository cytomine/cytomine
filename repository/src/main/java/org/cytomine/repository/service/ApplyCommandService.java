package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.common.repository.model.command.request.CreateTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermCommand;
import be.cytomine.common.repository.model.command.request.DeleteTermRelationCommand;
import be.cytomine.common.repository.model.command.request.UpdateTermCommand;

@Component
@AllArgsConstructor
public class ApplyCommandService {
    private final CommandV2Repository commandRepository;
    private final TermCommandService termCommandService;
    private final TermRelationCommandService termRelationCommandService;

    @Transactional
    public Optional<HttpCommandResponse> undoCommand(long userId, UUID undoCommand, LocalDateTime now) {
        return commandRepository.findById(undoCommand)

            .flatMap(commandEntity -> switch (commandEntity.getData()) {
                case DeleteTermCommand dtc ->
                    termCommandService.undoDeleteTerm(commandEntity.getId(), dtc, userId, now);
                case CreateTermCommand icr ->
                    termCommandService.undoCreateTerm(commandEntity.getId(), icr, userId, now);
                case UpdateTermCommand ucr -> termCommandService.undoUpdateTerm(commandEntity.getId(), ucr, userId);
                case DeleteTermRelationCommand deleteTermRelationCommand ->
                    termRelationCommandService.undoDeleteTermRelation(commandEntity.getId(), deleteTermRelationCommand, userId,
                        now);
            });
    }

    public Optional<HttpCommandResponse> redoCommand(long userId, UUID redoCommand, LocalDateTime now) {
        return commandRepository.findById(redoCommand).flatMap(commandEntity -> switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> termCommandService.redoDeleteTerm(commandEntity.getId(), dtc, userId, now);
            case CreateTermCommand icr -> termCommandService.redoCreateTerm(commandEntity.getId(), icr, userId, now);
            case UpdateTermCommand ucr -> termCommandService.redoUpdateTerm(commandEntity.getId(), ucr, userId, now);
            case DeleteTermRelationCommand ucr ->
                termRelationCommandService.redoDeleteTermRelation(commandEntity.getId(), ucr, userId, now);
        });
    }


}
