package be.cytomine.dto.annotation;

import java.util.Map;

public record GeoJsonFeature(
    String type,
    GeoJsonFeatureProperties properties,
    Map<String, Object> geometry
) {
    public GeoJsonFeature(GeoJsonFeatureProperties properties, Map<String, Object> geometry) {
        this("Feature", properties, geometry);
    }
}
