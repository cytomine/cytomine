package be.cytomine.dto.annotation;

import java.util.List;

public record GeoJsonFeatureCollection(
    String type,
    String name,
    List<GeoJsonFeature> features
) {

    public GeoJsonFeatureCollection(String name, List<GeoJsonFeature> features) {
        this("FeatureCollection", name, features);
    }
}
