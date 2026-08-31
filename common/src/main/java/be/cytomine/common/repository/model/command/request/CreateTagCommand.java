package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TagCommandPayload;

import static java.lang.String.format;

public record CreateTagCommand(TagCommandPayload after, long userId)
    implements CreateCommandRequest<TagCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_TAG_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Tag %s (name=%s) added", after.id(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_TAG;
    }
}
