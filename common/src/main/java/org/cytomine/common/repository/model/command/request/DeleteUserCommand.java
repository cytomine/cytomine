package org.cytomine.common.repository.model.command.request;

import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;
import org.cytomine.common.repository.model.command.payload.request.UserCommandPayload;

import static java.lang.String.format;

public record DeleteUserCommand(UserCommandPayload before, long userId)
    implements DeleteCommandRequest<UserCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_USER_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("User deleted, with id %s and username %s", before.id(), before.username());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_USER;
    }
}
