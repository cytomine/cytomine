package be.cytomine.common.repository.model.command;

public record HttpCommandResponse<T>(String message, Callback callback, boolean printMessage, T data, long command) {
}
