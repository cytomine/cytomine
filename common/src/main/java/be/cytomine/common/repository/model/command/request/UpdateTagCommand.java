package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TagCommandPayload;

import static java.lang.String.format;

public record UpdateTagCommand(TagCommandPayload before, TagCommandPayload after, long userId)
    implements UpdateCommandRequest<TagCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_TAG_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Tag %s updated (name: %s => %s)", before.id(), before.name(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_TAG;
    }
}
