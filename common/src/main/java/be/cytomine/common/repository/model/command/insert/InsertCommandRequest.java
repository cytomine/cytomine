package be.cytomine.common.repository.model.command.insert;

public sealed interface InsertCommandRequest<T> permits InsertTermCommand {

    T data();

    Long userId();

    Long projectId();

    String serviceName();
}
