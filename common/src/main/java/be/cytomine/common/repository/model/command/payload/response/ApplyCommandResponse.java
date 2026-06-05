package be.cytomine.common.repository.model.command.payload.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.HasLongId;
import be.cytomine.common.repository.model.HasLocaleDateTimeCUD;
import be.cytomine.common.repository.model.command.DataType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "dataType")
@JsonSubTypes({@JsonSubTypes.Type(value = TermResponse.class, name = "TERM"),
    @JsonSubTypes.Type(value = TermRelationResponse.class, name = "TERM_RELATION"),
    @JsonSubTypes.Type(value = OntologyResponse.class, name = "ONTOLOGY")})
public sealed interface ApplyCommandResponse extends HasLongId, HasLocaleDateTimeCUD
    permits OntologyResponse, TermRelationResponse, TermResponse {
    DataType getDataType();
}
