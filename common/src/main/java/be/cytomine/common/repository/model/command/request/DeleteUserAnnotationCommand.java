package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UserAnnotationCommandPayload;

import static java.lang.String.format;

public record DeleteUserAnnotationCommand(Long id, UserAnnotationCommandPayload before, long userId)
    implements DeleteCommandRequest<UserAnnotationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_USER_ANNOTATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("User annotation %s deleted", id);
    }
}
