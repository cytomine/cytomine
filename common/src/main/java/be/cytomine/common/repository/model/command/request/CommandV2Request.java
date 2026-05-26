package be.cytomine.common.repository.model.command.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.command.CommandType;
import be.cytomine.common.repository.model.command.payload.request.UpdateCommandPayload;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "commandType",
    visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = CreateTermCommand.class, name = "INSERT_TERM_COMMAND"),
    @JsonSubTypes.Type(value = UpdateTermCommand.class, name = "UPDATE_TERM_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTermCommand.class, name = "DELETE_TERM_COMMAND"),
    @JsonSubTypes.Type(value = CreateTermRelationCommand.class, name = "INSERT_TERM_RELATION_COMMAND"),
    @JsonSubTypes.Type(value = UpdateTermRelationCommand.class, name = "UPDATE_TERM_RELATION_COMMAND"),
    @JsonSubTypes.Type(value = DeleteTermRelationCommand.class, name = "DELETE_TERM_RELATION_COMMAND")})
public sealed interface CommandV2Request<T> permits DeleteCommandRequest, CreateCommandRequest, UpdateCommandRequest {
    UpdateCommandPayload<T> data();

    long userId();

    CommandType getCommandType();

    String getActionMessage();
}
