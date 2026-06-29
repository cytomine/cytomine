package be.cytomine.common.repository.model.command.request;

import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public record UndoCommandRequest<T extends HasLongId & HasAclId>(CommandV2Request<T> command,
                                                                 UUID commandId) implements CommandV2Request<T> {

    @Override
    public UpdateCommandPayload<T> data() {
        return command.data();
    }

    @Override
    public long userId() {
        return command.userId();
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.UNDO_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return String.format("Undoing command with id %s", commandId);
    }

    @Override
    public long id() {
        return command.id();
    }

    @Override
    public String getCommand() {
        return command.getCommand();
    }
}
