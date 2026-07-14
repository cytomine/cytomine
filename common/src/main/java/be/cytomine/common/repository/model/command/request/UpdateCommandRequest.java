package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface UpdateCommandRequest<T extends HasLongId & HasAclId> extends CommandV2Request<T>
    permits UpdateOntologyCommand, UpdateRoleCommand, UpdateTagDomainAssociationCommand, UpdateTermCommand,
    UpdateTermRelationCommand, UpdateStorageCommand, UpdateUploadedFileCommand, UpdateUserRoleCommand,
    UpdateUserCommand {

    T before();

    T after();

    @Override
    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.of(before()), Optional.of(after()));
    }

    default long id() {
        return before().id();
    }

    default long aclId() {
        return before().aclId();
    }
}
