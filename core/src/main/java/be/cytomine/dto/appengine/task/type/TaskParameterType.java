package be.cytomine.dto.appengine.task.type;

public sealed interface TaskParameterType permits
    BooleanType,
    CollectionType,
    DateTimeType,
    EnumerationType,
    FileType,
    GeometryType,
    ImageType,
    IntegerType,
    NumberType,
    StringType {
    String id();
}
