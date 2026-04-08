package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

import static be.cytomine.common.repository.model.command.CommandType.DELETE_COMMAND;

public sealed interface DeleteCommandRequest<T> extends CommandV2Request<T>
    permits DeleteTermCommand {
    @Override
    default CommandType getCommandType() {
        return DELETE_COMMAND;
    }

    T before();

    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.of(before()), Optional.empty());
    }
}
