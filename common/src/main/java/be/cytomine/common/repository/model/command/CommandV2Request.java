package be.cytomine.common.repository.model.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "commandType",
    visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = InsertTermCommand.class, name = "INSERT_COMMAND"),
    @JsonSubTypes.Type(value = UpdateTermCommand.class, name = "UPDATE_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTermCommand.class, name = "DELETE_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTermCommand.class, name = "DELETE_COMMAND")})
public sealed interface CommandV2Request<T> permits DeleteCommandRequest, InsertCommandRequest, UpdateCommandRequest {
    T data();

    long userId();

    DataType getDataType();

    CommandType getCommandType();

    String getActionMessage();
}
