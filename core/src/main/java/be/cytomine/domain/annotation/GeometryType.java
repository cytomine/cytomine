package be.cytomine.domain.annotation;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;

@Getter
@RequiredArgsConstructor
public enum GeometryType {
    POINT("Point"),
    MULTI_POINT("MultiPoint"),
    LINE_STRING("LineString"),
    MULTI_LINE_STRING("MultiLineString"),
    POLYGON("Polygon"),
    MULTI_POLYGON("MultiPolygon");

    private final String label;

    public static boolean isSupported(Geometry geometry) {
        return Arrays.stream(values())
            .anyMatch(t -> t.label.equals(geometry.getGeometryType()));
    }
}
