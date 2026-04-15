package be.cytomine.common.repository.model.command.payload.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import be.cytomine.common.repository.model.command.DataType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "dataType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TermResponse.class, name = "TERM"),
    @JsonSubTypes.Type(value = TermRelationResponse.class, name = "TERM_RELATION"),
    @JsonSubTypes.Type(value = AnnotationTermResponse.class, name = "ANNOTATION_TERM"),
    @JsonSubTypes.Type(value = UserAnnotationResponse.class, name = "USER_ANNOTATION"),
    @JsonSubTypes.Type(value = ReviewedAnnotationResponse.class, name = "REVIEWED_ANNOTATION")
})
public sealed interface ApplyCommandResponse
    permits TermRelationResponse, TermResponse,
        AnnotationTermResponse, UserAnnotationResponse, ReviewedAnnotationResponse {
    DataType getDataType();
}
