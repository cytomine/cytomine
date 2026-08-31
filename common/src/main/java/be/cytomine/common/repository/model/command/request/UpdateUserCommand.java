package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.UserCommandPayload;

import static java.lang.String.format;

public record UpdateUserCommand(UserCommandPayload before, UserCommandPayload after, long userId)
    implements UpdateCommandRequest<UserCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_USER_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("User %s updated, (username: %s => %s)", after.id(), before.username(), after.username());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_USER;
    }
}
