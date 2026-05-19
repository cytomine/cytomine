package be.cytomine.dto.appengine.task.output;

public sealed interface TaskRunOutput permits
    BooleanOutput,
    DateTimeOutput,
    EnumerationOutput,
    GeometryOutput,
    IntegerOutput,
    NumberOutput,
    StringOutput {
}
