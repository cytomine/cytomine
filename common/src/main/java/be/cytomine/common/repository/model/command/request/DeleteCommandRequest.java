package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface DeleteCommandRequest<T> extends CommandV2Request<T>
    permits DeleteReviewedAnnotationCommand, DeleteTermCommand, DeleteTermRelationCommand {

    T before();

    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.of(before()), Optional.empty());
    }
}
