package be.cytomine.common.repository.model.command;

import java.util.UUID;

public record HttpCommandResponse<T>(Callback callback, boolean printMessage, T data, UUID command) {
}
