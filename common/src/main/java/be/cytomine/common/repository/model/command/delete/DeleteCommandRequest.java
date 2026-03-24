package be.cytomine.common.repository.model.command.delete;

public sealed  interface DeleteCommandRequest<T> permits DeleteTermCommand {

    T data();

    Long userId();

    Long projectId();

    String serviceName();
}
