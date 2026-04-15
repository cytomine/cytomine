package be.cytomine.common.repository.model.command.request;

import java.util.Optional;

import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

public sealed interface CreateCommandRequest<T> extends CommandV2Request<T>
    permits CreateTermCommand, CreateTermRelationCommand,
        CreateAnnotationTermCommand, CreateUserAnnotationCommand, CreateReviewedAnnotationCommand {

    T after();

    default UpdateCommandPayload<T> data() {
        return new UpdateCommandPayload<>(Optional.empty(), Optional.of(after()));
    }
}
