package be.cytomine.service.utils;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import be.cytomine.domain.annotation.GeometryType;

import static org.assertj.core.api.Assertions.assertThat;

public class GeometryServiceTests {

    private final GeometryService geometryService = new GeometryService(new GeoJsonWriter());

    @Nested
    class ParseTests {

        @Test
        void shouldReturnGeometryForValidWkt() {
            String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

            Optional<Geometry> result = geometryService.parse(wkt);

            assertThat(result).isPresent();
            assertThat(result.get().getGeometryType()).isEqualTo(GeometryType.POLYGON.getLabel());
        }

        @Test
        void shouldReturnGeometryForValidGeoJson() {
            String geoJson = """
                {
                  "type": "Polygon",
                  "coordinates": [[[0,0],[1,0],[1,1],[0,1],[0,0]]]
                }
                """;

            Optional<Geometry> result = geometryService.parse(geoJson);

            assertThat(result).isPresent();
            assertThat(result.get().getGeometryType()).isEqualTo(GeometryType.POLYGON.getLabel());
        }

        @Test
        void shouldReturnEmptyForInvalidInput() {
            String invalid = "not a geometry";

            Optional<Geometry> result = geometryService.parse(invalid);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyForEmptyString() {
            String empty = "";

            Optional<Geometry> result = geometryService.parse(empty);

            assertThat(result).isEmpty();
        }
    }
}
