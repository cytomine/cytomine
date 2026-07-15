package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.TagCommandPayload;

import static java.lang.String.format;

public record DeleteTagCommand(TagCommandPayload before, long userId) implements DeleteCommandRequest<TagCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_TAG_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Tag %s (name=%s) deleted", before.id(), before.name());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_TAG;
    }
}
