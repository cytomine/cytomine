package be.cytomine.service.utils;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import be.cytomine.domain.annotation.GeometryType;

import static org.assertj.core.api.Assertions.assertThat;

public class GeometryServiceTests {
    private final GeometryService geometryService = new GeometryService(new GeoJsonWriter());

    @Test
    public void shouldReturnGeometryForValidWkt() {
        String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

        Optional<Geometry> result = geometryService.parse(wkt);

        assertThat(result).isPresent();
        assertThat(result.get().getGeometryType()).isEqualTo(GeometryType.POLYGON.getLabel());
    }
}
