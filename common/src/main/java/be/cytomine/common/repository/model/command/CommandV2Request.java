package be.cytomine.common.repository.model.command;

import java.util.Optional;

public sealed interface CommandV2Request<T> permits DeleteCommandRequest, InsertCommandRequest, UpdateCommandRequest {
    T data();

    Long userId();

    Optional<Long> projectId();

    DataType dataType();

    CommandType commandType();

    String getActionMessage();
}
