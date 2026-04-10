package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UserAnnotationCommandPayload;

import static java.lang.String.format;

public record UpdateUserAnnotationCommand(Long id, UserAnnotationCommandPayload before,
                                          UserAnnotationCommandPayload after, long userId)
    implements UpdateCommandRequest<UserAnnotationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_USER_ANNOTATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("User annotation %s updated", id);
    }
}
