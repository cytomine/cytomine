package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.StorageCommandPayload;

import static java.lang.String.format;

public record UpdateStorageCommand(StorageCommandPayload before, StorageCommandPayload after, long userId)
    implements UpdateCommandRequest<StorageCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_STORAGE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Storage %s updated, (name: %s => %s)", after.id(), before.name(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_STORAGE;
    }
}
