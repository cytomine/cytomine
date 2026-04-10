package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UserAnnotationCommandPayload;

import static java.lang.String.format;

public record CreateUserAnnotationCommand(UserAnnotationCommandPayload after, long userId)
    implements CreateCommandRequest<UserAnnotationCommandPayload> {

    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_USER_ANNOTATION_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("User annotation %s added in image %s by user %s",
            after.id(), after.imageId(), after.userId());
    }
}
