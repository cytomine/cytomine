package be.cytomine.common.repository.model.command.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.DataType;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "commandType",
    visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateTermCommand.class, name = "INSERT_COMMAND"),
    @JsonSubTypes.Type(value = UpdateTermCommand.class, name = "UPDATE_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTermCommand.class, name = "DELETE_COMMAND")})
public sealed interface CommandV2Request<T> permits DeleteCommandRequest, CreateCommandRequest, UpdateCommandRequest {
    UpdateCommandPayload<T> data();

    long userId();

    DataType getDataType();

    CommandType getCommandType();

    String getActionMessage();
}
