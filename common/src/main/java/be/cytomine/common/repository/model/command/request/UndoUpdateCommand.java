package be.cytomine.common.repository.model.command.request;

import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;

public record UndoUpdateCommand<T extends HasLongId & HasAclId>(UpdateCommandRequest<T> target,
                                                                UUID commandId) implements UndoCommandRequest<T> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UNDO_UPDATE_COMMAND;
    }

    @Override
    public String getCommand() {
        return Commands.UNDO_UPDATE_COMMAND;
    }
}
