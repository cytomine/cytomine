package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.RoleCommandPayload;

import static java.lang.String.format;

public record DeleteRoleCommand(RoleCommandPayload before, long userId)
    implements DeleteCommandRequest<RoleCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_ROLE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("SecRole deleted, with id %s and authority %s", before.id(), before.authority());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_ROLE;
    }
}
