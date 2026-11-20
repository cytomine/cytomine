package org.cytomine.common.repository.model.command.request;

import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;
import org.cytomine.common.repository.model.command.payload.request.UserRoleCommandPayload;

import static java.lang.String.format;

public record UpdateUserRoleCommand(UserRoleCommandPayload before, UserRoleCommandPayload after, long userId)
    implements UpdateCommandRequest<UserRoleCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_USER_ROLE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("SecUserSecRole %s updated (roleId: %s => %s)", after.id(), before.roleId(), after.roleId());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_USER_ROLE;
    }
}
