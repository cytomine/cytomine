package be.cytomine.dto.appengine.task.output;

public sealed interface TaskRunOutput permits
    BooleanOutput,
    CollectionOutput,
    DateTimeOutput,
    EnumerationOutput,
    FileOutput,
    GeometryOutput,
    ImageOutput,
    IntegerOutput,
    NumberOutput,
    StringOutput {
}
