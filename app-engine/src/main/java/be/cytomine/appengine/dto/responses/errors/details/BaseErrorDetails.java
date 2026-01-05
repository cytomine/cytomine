package be.cytomine.appengine.dto.responses.errors.details;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BatchError.class, name = "batch"),
    @JsonSubTypes.Type(value = EmptyErrorDetails.class, name = "empty"),
    @JsonSubTypes.Type(value = MultipleErrors.class, name = "multiple"),
    @JsonSubTypes.Type(value = ParameterError.class, name = "parameter"),
    @JsonSubTypes.Type(value = ParamRelatedError.class, name = "related"),
    @JsonSubTypes.Type(value = ServerError.class, name = "server"),
})
public class BaseErrorDetails {
}
