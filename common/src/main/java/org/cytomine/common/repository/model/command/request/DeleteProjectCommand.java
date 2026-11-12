package org.cytomine.common.repository.model.command.request;

import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;
import org.cytomine.common.repository.model.command.payload.request.ProjectCommandPayload;

import static java.lang.String.format;

public record DeleteProjectCommand(ProjectCommandPayload before, long userId)
    implements DeleteCommandRequest<ProjectCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_PROJECT_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Project %s (name=%s) deleted", before.id(), before.name());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_PROJECT;
    }
}
