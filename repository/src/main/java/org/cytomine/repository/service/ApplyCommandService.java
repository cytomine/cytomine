package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
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
    public Optional<HttpCommandResponse<TermResponse>> undoCommand(long userId, UUID undoCommand, LocalDateTime now) {
        return commandRepository.findById(undoCommand)

                   .flatMap(commandEntity -> switch (commandEntity.getData()) {
                       case DeleteTermCommand dtc ->
                           termCommandService.undoDeleteTerm(commandEntity.getId(), dtc, userId, now);
                       case CreateTermCommand icr ->
                           termCommandService.undoCreateTerm(commandEntity.getId(), icr, userId, now);
                       case UpdateTermCommand ucr ->
                           termCommandService.undoUpdateTerm(commandEntity.getId(), ucr, userId);
                   });
    }

    public Optional<HttpCommandResponse<TermResponse>> redoCommand(long userId, UUID undoCommand, LocalDateTime now) {
        return commandRepository.findById(undoCommand)
                   .flatMap(commandEntity -> switch (commandEntity.getData()) {
                       case DeleteTermCommand dtc ->
                           termCommandService.redoDeleteTerm(commandEntity.getId(), dtc, userId, now);
                       case CreateTermCommand icr ->
                           termCommandService.redoCreateTerm(commandEntity.getId(), icr, userId, now);
                       case UpdateTermCommand ucr ->
                           termCommandService.redoUpdateTerm(commandEntity.getId(), ucr, userId, now);
                   });
    }


}
