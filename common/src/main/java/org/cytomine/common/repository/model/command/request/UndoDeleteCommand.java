package org.cytomine.common.repository.model.command.request;

import java.util.UUID;

import org.cytomine.common.repository.model.HasAclId;
import org.cytomine.common.repository.model.HasLongId;
import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;

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
