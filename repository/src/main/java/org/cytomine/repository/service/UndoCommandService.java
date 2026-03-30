package org.cytomine.repository.service;

import java.util.Optional;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.DeleteTermCommand;
import be.cytomine.common.repository.model.command.InsertTermCommand;
import be.cytomine.common.repository.model.command.UpdateTermCommand;

@Component
@AllArgsConstructor
public class UndoCommandService {
    private final CommandV2Repository commandRepository;
    private final ACLService aclService;
    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;

    public Optional<Long> undoCommand(long userId, UUID undoCommand) {
        return commandRepository.findById(undoCommand)
                   .filter(commandEntity -> userCanUndoCommand(userId, commandEntity))
                   .flatMap(commandEntity -> switch (commandEntity.getData()) {
                       case DeleteTermCommand dtc -> undoDeleteTermCommand(dtc);
                       case InsertTermCommand icr -> undoInsertTermCommand(icr);
                       case UpdateTermCommand ucr -> undoUpdateTermCommand(ucr);
                   });
    }

    boolean userCanUndoCommand(long userId, CommandV2Entity commandEntity) {
        return switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> aclService.canWriteOntology(userId, dtc.ontologyId());
            case InsertTermCommand icr -> aclService.canWriteOntology(userId, icr.ontologyId());
            case UpdateTermCommand ucr -> aclService.canWriteOntology(userId, ucr.ontologyId());
        };
    }

    Optional<Long> undoDeleteTermCommand(DeleteTermCommand dtc) {
        TermEntity termEntity = ontologyMapper.mapToTermEntityWithoutID(dtc.before());
        return Optional.of(termRepository.save(termEntity).getId());
    }

    Optional<Long> undoInsertTermCommand(InsertTermCommand dtc) {
        termRepository.deleteById(dtc.after().id());
        return Optional.empty();
    }

    Optional<Long> undoUpdateTermCommand(UpdateTermCommand dtc) {
        termRepository.save(ontologyMapper.mapToTermEntity(dtc.before()));
        return Optional.of(dtc.before().id());
    }

}
