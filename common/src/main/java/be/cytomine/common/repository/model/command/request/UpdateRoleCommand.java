package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.RoleCommandPayload;

import static java.lang.String.format;

public record UpdateRoleCommand(RoleCommandPayload before, RoleCommandPayload after, long userId)
    implements UpdateCommandRequest<RoleCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_ROLE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("SecRole %s updated, (authority: %s => %s)", after.id(), before.authority(), after.authority());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_ROLE;
    }
}
