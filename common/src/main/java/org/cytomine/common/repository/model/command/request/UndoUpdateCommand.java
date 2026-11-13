package org.cytomine.common.repository.model.command.request;

import java.util.UUID;

import org.cytomine.common.repository.model.HasAclId;
import org.cytomine.common.repository.model.HasLongId;
import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;

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
