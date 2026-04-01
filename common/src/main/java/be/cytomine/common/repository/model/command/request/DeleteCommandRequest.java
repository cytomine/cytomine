package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;

import static be.cytomine.common.repository.model.command.CommandType.DELETE_COMMAND;

public sealed interface DeleteCommandRequest<T> extends CommandV2Request<T> permits DeleteTermCommand {
    @Override
    default CommandType getCommandType() {
        return DELETE_COMMAND;
    }
}
