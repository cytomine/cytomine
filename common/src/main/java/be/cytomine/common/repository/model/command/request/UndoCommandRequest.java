package be.cytomine.common.repository.model.command.request;

import java.util.UUID;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface UndoCommandRequest<T extends HasLongId & HasAclId> extends CommandV2Request<T>
    permits UndoCreateCommand, UndoDeleteCommand, UndoUpdateCommand {
    CommandV2Request<T> target();

    UUID commandId();

    @Override
    default UpdateCommandPayload<T> data() {
        return target().data();
    }

    @Override
    default long userId() {
        return target().userId();
    }

    @Override
    default String getActionMessage() {
        return String.format("Undoing target with id %s", commandId());
    }

    @Override
    default long id() {
        return target().id();
    }

}
