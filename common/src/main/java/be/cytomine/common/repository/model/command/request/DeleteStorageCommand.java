package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.StorageCommandPayload;

import static java.lang.String.format;

public record DeleteStorageCommand(StorageCommandPayload before, long userId)
    implements DeleteCommandRequest<StorageCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_STORAGE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Storage deleted, with id %s and name %s", before.id(), before.name());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_STORAGE;
    }
}
