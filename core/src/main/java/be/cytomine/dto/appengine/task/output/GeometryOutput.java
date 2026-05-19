package be.cytomine.dto.appengine.task.output;

import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

public record GeometryOutput(
    UUID taskRunId,
    String parameterName,
    String type,
    Geometry value
) implements TaskRunOutput {}
