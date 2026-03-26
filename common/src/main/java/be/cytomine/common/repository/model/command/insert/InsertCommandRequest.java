package be.cytomine.common.repository.model.command.insert;

import java.util.Optional;

public sealed interface InsertCommandRequest<T> permits InsertTermCommand {
    T data();

    Long userId();

    Optional<Long> projectId();

    String serviceName();

    String getActionMessage();
}
