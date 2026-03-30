package be.cytomine.common.repository.model.command;

import static be.cytomine.common.repository.model.command.CommandType.UPDATE_COMMAND;

public sealed interface UpdateCommandRequest<T> extends CommandV2Request<T> permits UpdateTermCommand {
    @Override
    default CommandType getCommandType() {
        return UPDATE_COMMAND;
    }
}
