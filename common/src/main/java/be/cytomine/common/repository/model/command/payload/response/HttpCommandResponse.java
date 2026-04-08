package be.cytomine.common.repository.model.command.payload.response;

import java.util.UUID;

public record HttpCommandResponse(boolean printMessage, ApplyCommandResponse data, UUID commandId, String command) {
}
