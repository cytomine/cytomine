package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface CreateCommandRequest<T extends HasLongId & HasAclId> extends CommandV2Request<T>
    permits CreateOntologyCommand, CreateTermCommand, CreateTermRelationCommand {

    T after();

    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.empty(), Optional.of(after()));
    }

    default long id() {
        return after().id();
    }

    default long aclId() {
        return after().aclId();
    }
}
