package be.cytomine.common.repository.model.command.payload.response;

import java.util.UUID;

/**
 * This payload is the common response to use with the APIs
 */
public record HttpCommandResponse(boolean printMessage, ApplyCommandResponse data, UUID commandId, String command) {
}
