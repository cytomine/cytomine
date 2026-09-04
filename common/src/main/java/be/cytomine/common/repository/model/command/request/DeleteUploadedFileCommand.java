package be.cytomine.common.repository.model.command.request;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.request.UploadedFileCommandPayload;

import static java.lang.String.format;

public record DeleteUploadedFileCommand(UploadedFileCommandPayload before, long userId)
    implements DeleteCommandRequest<UploadedFileCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE_UPLOADED_FILE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("UploadedFile deleted, with id %s and filename %s", before.id(), before.filename());
    }

    @Override
    public String getCommand() {
        return Commands.DELETE_UPLOADED_FILE;
    }
}
