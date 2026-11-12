package org.cytomine.common.repository.model.command.request;

import java.util.Optional;

import org.cytomine.common.repository.model.HasAclId;
import org.cytomine.common.repository.model.HasLongId;
import org.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface CreateCommandRequest<T extends HasLongId & HasAclId> extends CommandV2Request<T>
    permits CreateOntologyCommand,
    CreateProjectCommand,
    CreateRoleCommand,
    CreateStorageCommand,
    CreateTagCommand,
    CreateTagDomainAssociationCommand,
    CreateTermCommand,
    CreateTermRelationCommand,
    CreateUploadedFileCommand,
    CreateUserCommand,
    CreateUserRoleCommand {

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
