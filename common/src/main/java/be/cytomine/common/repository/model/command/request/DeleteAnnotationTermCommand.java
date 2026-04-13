package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.AnnotationTermCommandPayload;

import static java.lang.String.format;

public record DeleteAnnotationTermCommand(Long id, AnnotationTermCommandPayload before, long userId)
    implements DeleteCommandRequest<AnnotationTermCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_ANNOTATION_TERM_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("Annotation term %s (annotation: %s, term: %s) deleted",
            before.id(), before.userAnnotationId(), before.termId());
    }
}
