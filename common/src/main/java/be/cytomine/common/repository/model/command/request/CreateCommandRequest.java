package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;

import static be.cytomine.common.repository.model.command.CommandType.INSERT_COMMAND;

public sealed interface CreateCommandRequest<T> extends CommandV2Request<T> permits CreateTermCommand {
    @Override
    default CommandType getCommandType() {
        return INSERT_COMMAND;
    }
}
