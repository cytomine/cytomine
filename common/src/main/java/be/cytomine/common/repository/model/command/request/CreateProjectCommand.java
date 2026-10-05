package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.ProjectCommandPayload;

import static java.lang.String.format;

public record CreateProjectCommand(ProjectCommandPayload after, long userId)
    implements CreateCommandRequest<ProjectCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_PROJECT_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Project %s (name=%s) added", after.id(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_PROJECT;
    }
}
