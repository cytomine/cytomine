package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.UploadedFileCommandPayload;

import static java.lang.String.format;

public record CreateUploadedFileCommand(UploadedFileCommandPayload after, long userId)
    implements CreateCommandRequest<UploadedFileCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_UPLOADED_FILE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("UploadedFile created, with id %s and filename %s", after.id(), after.filename());
    }

    @Override
    public String getCommand() {
        return Commands.CREATE_UPLOADED_FILE;
    }
}
