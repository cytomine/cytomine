package be.cytomine.common.repository.model.command;

import java.util.UUID;

import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;

public record HttpCommandResponse(boolean printMessage, ApplyCommandResponse data, UUID command) {
}
