package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.UserRoleCommandPayload;

import static java.lang.String.format;

public record CreateUserRoleCommand(UserRoleCommandPayload after, long userId)
    implements CreateCommandRequest<UserRoleCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_USER_ROLE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("SecUserSecRole created, with id %s for user %s with role %s",
            after.id(), after.userId(), after.roleId());

    }

    @Override
    public String getCommand() {
        return Commands.CREATE_USER_ROLE;
    }
}
