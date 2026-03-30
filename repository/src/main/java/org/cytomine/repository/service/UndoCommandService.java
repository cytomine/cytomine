package org.cytomine.repository.service;

import lombok.AllArgsConstructor;
import org.cytomine.repository.persistence.CommandV2Repository;
import org.cytomine.repository.persistence.entity.CommandV2Entity;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.api.UndoCommand;

@Component
@AllArgsConstructor
public class UndoCommandService {
    private final CommandV2Repository commandRepository;
    private final ACLService aclService;

    public boolean undoCommand(long userId, UndoCommand undoCommand) {
        return commandRepository.findById(undoCommand.id())
                   .filter(commandEntity -> userCanUndoCommand(userId, commandEntity))
                   .map(commandEntity -> commandEntity
                   ).isPresent();
    }

    private boolean userCanUndoCommand(long userId, CommandV2Entity commandEntity) {


        return true;
    }

}
