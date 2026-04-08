package be.cytomine.common.repository.model.command.payload.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.command.DataType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "dataType")
@JsonSubTypes({@JsonSubTypes.Type(value = TermResponse.class, name = "TERM")})
public sealed interface ApplyCommandResponse permits TermResponse {
    DataType getDataType();
}
