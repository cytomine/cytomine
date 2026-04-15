package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.ReviewedAnnotationCommandPayload;

import static java.lang.String.format;

public record CreateReviewedAnnotationCommand(ReviewedAnnotationCommandPayload after, long userId)
    implements CreateCommandRequest<ReviewedAnnotationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_REVIEWED_ANNOTATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Reviewed annotation %s added in image %s", after.id(), after.imageId());
    }
}
