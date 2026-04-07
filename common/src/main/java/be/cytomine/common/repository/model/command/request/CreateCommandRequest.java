package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

import static be.cytomine.common.repository.model.command.CommandType.INSERT_COMMAND;

public sealed interface CreateCommandRequest<T> extends CommandV2Request<UpdateCommandPayload<T>>
    permits CreateTermCommand {
    @Override
    default CommandType getCommandType() {
        return INSERT_COMMAND;
    }

    T after();

    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.empty(), Optional.of(after()));
    }
}
