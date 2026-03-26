package be.cytomine.common.repository.model.command.update;

import java.util.Optional;

public sealed interface UpdateCommandRequest<T> permits UpdateTermCommand {
    T data();

    Long userId();

    Optional<Long> projectId();

    String serviceName();

    String getActionMessage();
}
