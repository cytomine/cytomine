package be.cytomine.dto.appengine.task.output;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BooleanOutput.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = DateTimeOutput.class, name = "DATETIME"),
    @JsonSubTypes.Type(value = EnumerationOutput.class, name = "ENUMERATION"),
    @JsonSubTypes.Type(value = GeometryOutput.class, name = "GEOMETRY"),
    @JsonSubTypes.Type(value = IntegerOutput.class, name = "INTEGER"),
    @JsonSubTypes.Type(value = NumberOutput.class, name = "NUMBER"),
    @JsonSubTypes.Type(value = StringOutput.class, name = "STRING")
})
public abstract class TaskRunOutputMixin {}
