package be.cytomine.common.repository.model.command.update;

public sealed interface UpdateCommandRequest<T> permits UpdateTermCommand {

    T data();

    Long userId();

    Long projectId();

    String serviceName();
}
