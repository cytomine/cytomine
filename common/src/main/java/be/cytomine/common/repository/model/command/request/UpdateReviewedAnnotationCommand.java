package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.ReviewedAnnotationCommandPayload;

import static java.lang.String.format;

public record UpdateReviewedAnnotationCommand(Long id, ReviewedAnnotationCommandPayload before,
                                              ReviewedAnnotationCommandPayload after, long userId)
    implements UpdateCommandRequest<ReviewedAnnotationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_REVIEWED_ANNOTATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Reviewed annotation %s updated", id);
    }
}
