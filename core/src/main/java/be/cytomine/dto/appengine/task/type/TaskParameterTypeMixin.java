package be.cytomine.dto.appengine.task.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "id", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanType.class, name = "boolean"),
        @JsonSubTypes.Type(value = CollectionType.class, name = "array"),
        @JsonSubTypes.Type(value = DateTimeType.class, name = "datetime"),
        @JsonSubTypes.Type(value = EnumerationType.class, name = "enumeration"),
        @JsonSubTypes.Type(value = FileType.class, name = "file"),
        @JsonSubTypes.Type(value = GeometryType.class, name = "geometry"),
        @JsonSubTypes.Type(value = ImageType.class, name = "image"),
        @JsonSubTypes.Type(value = IntegerType.class, name = "integer"),
        @JsonSubTypes.Type(value = NumberType.class, name = "number"),
        @JsonSubTypes.Type(value = StringType.class, name = "string")
})
public abstract class TaskParameterTypeMixin {}
