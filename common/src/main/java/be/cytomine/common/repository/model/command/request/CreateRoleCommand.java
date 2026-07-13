package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.RoleCommandPayload;

import static java.lang.String.format;

public record CreateRoleCommand(RoleCommandPayload after, long userId)
    implements CreateCommandRequest<RoleCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_ROLE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("SecRole created, with id %s and authority %s", after.id(), after.authority());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_ROLE;
    }
}
