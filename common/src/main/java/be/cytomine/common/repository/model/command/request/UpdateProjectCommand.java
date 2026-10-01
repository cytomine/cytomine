package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.ProjectCommandPayload;

import static java.lang.String.format;

public record UpdateProjectCommand(ProjectCommandPayload before, ProjectCommandPayload after, long userId)
    implements UpdateCommandRequest<ProjectCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_PROJECT_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Project %s updated (name: %s => %s)", before.id(), before.name(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_PROJECT;
    }
}
