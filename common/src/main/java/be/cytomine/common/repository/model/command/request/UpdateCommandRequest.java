package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

import static be.cytomine.common.repository.model.command.CommandType.UPDATE_COMMAND;

public sealed interface UpdateCommandRequest<T> extends CommandV2Request<T>
    permits UpdateTermCommand {
    @Override
    default CommandType getCommandType() {
        return UPDATE_COMMAND;
    }

    T before();

    T after();

    @Override
    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.of(before()), Optional.of(after()));
    }
}
