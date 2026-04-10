package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.AnnotationTermCommandPayload;

import static java.lang.String.format;

public record CreateAnnotationTermCommand(AnnotationTermCommandPayload after, long userId)
    implements CreateCommandRequest<AnnotationTermCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_ANNOTATION_TERM_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Annotation term %s (annotation: %s, term: %s) added",
            after.id(), after.userAnnotationId(), after.termId());
    }
}
