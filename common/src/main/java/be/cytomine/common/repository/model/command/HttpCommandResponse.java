package be.cytomine.common.repository.model.command;

public record HttpCommandResponse<T>(Callback callback, boolean printMessage, T data, long command) {
}
