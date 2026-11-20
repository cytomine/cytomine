package org.cytomine.common.repository.model.command.request;

import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;
import org.cytomine.common.repository.model.command.payload.request.UserCommandPayload;

import static java.lang.String.format;

public record CreateUserCommand(UserCommandPayload after, long userId)
    implements CreateCommandRequest<UserCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_USER_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("User created, with id %s and username %s", after.id(), after.username());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_USER;
    }
}
