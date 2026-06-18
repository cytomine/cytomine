package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.HasAclId;
import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface DeleteCommandRequest<T extends HasLongId & HasAclId> extends CommandV2Request<T>
    permits DeleteOntologyCommand, DeleteTermCommand, DeleteTermRelationCommand, DeleteStorageCommand,
        DeleteUploadedFileCommand {

    T before();

    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.of(before()), Optional.empty());
    }

    default long id() {
        return before().id();
    }

    default long aclId() {
        return before().aclId();
    }
}
