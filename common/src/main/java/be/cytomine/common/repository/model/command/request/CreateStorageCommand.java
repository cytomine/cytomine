package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.StorageCommandPayload;

import static java.lang.String.format;

public record CreateStorageCommand(StorageCommandPayload after, long userId)
    implements CreateCommandRequest<StorageCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_STORAGE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Storage created, with id %s and name %s", after.id(), after.name());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_STORAGE;
    }
}
