package org.cytomine.repository.service;

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
import be.cytomine.common.repository.model.command.api.UndoCommand;

@Component
@AllArgsConstructor
public class UndoCommandService {
    private final CommandV2Repository commandRepository;
    private final ACLService aclService;
    private final TermRepository termRepository;
    private final OntologyMapper ontologyMapper;

    public boolean undoCommand(long userId, UndoCommand undoCommand) {
        return commandRepository.findById(undoCommand.id())
                   .filter(commandEntity -> userCanUndoCommand(userId, commandEntity))
                   .map(commandEntity -> switch (commandEntity.getData()) {
                           case DeleteTermCommand dtc -> undoDeleteTermCommand(dtc);
                           case InsertTermCommand icr -> undoInsertTermCommand(icr);
                           case UpdateTermCommand ucr -> undoUpdateTermCommand(ucr);
                       }
                   ).isPresent();
    }

    boolean userCanUndoCommand(long userId, CommandV2Entity commandEntity) {
        return switch (commandEntity.getData()) {
            case DeleteTermCommand dtc -> aclService.canWriteOntology(userId, dtc.ontologyId());
            case InsertTermCommand icr -> aclService.canWriteOntology(userId, icr.ontologyId());
            case UpdateTermCommand ucr -> aclService.canWriteOntology(userId, ucr.ontologyId());
        };
    }

    boolean undoDeleteTermCommand(DeleteTermCommand dtc) {
        TermEntity termEntity = ontologyMapper.mapToTermEntity(dtc.data());
        termRepository.save(termEntity);
        return true;
    }

    boolean undoInsertTermCommand(InsertTermCommand dtc) {
        return true;
    }

    boolean undoUpdateTermCommand(UpdateTermCommand dtc) {
        return true;
    }

}
