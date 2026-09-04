package be.cytomine.common.repository.model.command.request;

import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;

public record UndoCreateCommand<T extends HasLongId & HasAclId>(CreateCommandRequest<T> target,
                                                                UUID commandId) implements UndoCommandRequest<T> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UNDO_CREATE_COMMAND;
    }

    @Override
    public String getCommand() {
        return Commands.UNDO_CREATE_COMMAND;
    }
}
