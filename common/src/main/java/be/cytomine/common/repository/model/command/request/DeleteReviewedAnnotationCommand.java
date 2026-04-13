package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.ReviewedAnnotationCommandPayload;

import static java.lang.String.format;

public record DeleteReviewedAnnotationCommand(Long id, ReviewedAnnotationCommandPayload before, long userId)
    implements DeleteCommandRequest<ReviewedAnnotationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_REVIEWED_ANNOTATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Reviewed annotation %s deleted", id);
    }
}
