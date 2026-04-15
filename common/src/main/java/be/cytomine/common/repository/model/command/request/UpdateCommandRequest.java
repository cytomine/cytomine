package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface UpdateCommandRequest<T> extends CommandV2Request<T>
    permits UpdateReviewedAnnotationCommand, UpdateTermCommand, UpdateTermRelationCommand {

    T before();

    T after();

    @Override
    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.of(before()), Optional.of(after()));
    }
}
