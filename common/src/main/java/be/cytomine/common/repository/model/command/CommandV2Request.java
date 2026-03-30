package be.cytomine.common.repository.model.command;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "commandType",
    visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = InsertTermCommand.class, name = Commands.INSERT_TERM),
    @JsonSubTypes.Type(value = UpdateTermCommand.class, name = Commands.UPDATE_TERM),
    @JsonSubTypes.Type(value = DeleteTermCommand.class, name = Commands.DELETE_TERM),
})
public sealed interface CommandV2Request<T> permits DeleteCommandRequest, InsertCommandRequest, UpdateCommandRequest {
    T data();

    Long userId();

    Optional<Long> getProjectId();

    DataType getDataType();

    CommandType getCommandType();

    String getActionMessage();
}
