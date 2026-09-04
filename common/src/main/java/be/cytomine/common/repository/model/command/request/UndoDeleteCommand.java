package be.cytomine.common.repository.model.command.request;

import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;

public record UndoDeleteCommand<T extends HasLongId & HasAclId>(DeleteCommandRequest<T> target,
                                                                UUID commandId) implements UndoCommandRequest<T> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UNDO_DELETE_COMMAND;
    }

    @Override
    public String getCommand() {
        return Commands.UNDO_DELETE_COMMAND;
    }
}
