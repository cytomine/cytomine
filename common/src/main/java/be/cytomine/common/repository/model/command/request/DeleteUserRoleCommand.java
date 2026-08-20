package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.UserRoleCommandPayload;

import static java.lang.String.format;

public record DeleteUserRoleCommand(UserRoleCommandPayload before, long userId)
    implements DeleteCommandRequest<UserRoleCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_USER_ROLE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("SecUserSecRole deleted, with id %s for user %s with role %s",
            before.id(), before.userId(), before.roleId());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_USER_ROLE;
    }
}
