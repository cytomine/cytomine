package org.cytomine.common.repository.model.command.request;

import org.cytomine.common.repository.model.command.CommandType;
import org.cytomine.common.repository.model.command.Commands;
import org.cytomine.common.repository.model.command.payload.request.UploadedFileCommandPayload;

import static java.lang.String.format;

public record UpdateUploadedFileCommand(
    UploadedFileCommandPayload before,
    UploadedFileCommandPayload after,
    long userId
) implements UpdateCommandRequest<UploadedFileCommandPayload> {
    @Override
    public CommandType getCommandType() {
        return CommandType.UPDATE_UPLOADED_FILE_COMMAND;
    }

    @Override
    public String getActionMessage() {
        return format("UploadedFile %s updated, (filename: %s => %s)", after.id(), before.filename(), after.filename());
    }

    @Override
    public String getCommand() {
        return Commands.UPDATE_UPLOADED_FILE;
    }
}
