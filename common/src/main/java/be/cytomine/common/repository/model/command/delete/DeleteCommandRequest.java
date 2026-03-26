package be.cytomine.common.repository.model.command.delete;

import java.util.Optional;

public sealed interface DeleteCommandRequest<T> permits DeleteTermCommand {

    T data();

    Long userId();

    Optional<Long> projectId();
    Optional<Long> ontologyId();
    Optional<Long> storageId();

    String serviceName();
}
