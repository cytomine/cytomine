package be.cytomine.service.utils;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Envelope;
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

        @ParameterizedTest
        @ValueSource(strings = {
            "not a geometry",
            ""
        })
        void shouldReturnEmptyForInvalidInput(String invalid) {
            Optional<Geometry> result = geometryService.parse(invalid);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class IsGeometryTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "POINT (4.3517 50.8503)",
            "MULTIPOINT (1 1, 2 2)",
            "LINESTRING (0 0, 1 1)",
            "MULTILINESTRING ((0 0, 1 1), (1 1, 2 2))",
            "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))",
            "MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((2 2, 3 2, 3 3, 2 3, 2 2)))"
        })
        void shouldReturnTrueForValidWkt(String input) {
            boolean result = geometryService.isGeometry(input);

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnTrueForValidGeojsonPolygon() {
            String geoJson = """
                {
                  "type": "Polygon",
                  "coordinates": [[[0,0],[1,0],[1,1],[0,1],[0,0]]]
                }
                """;

            boolean result = geometryService.isGeometry(geoJson);

            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not a geometry",
            ""
        })
        void shouldReturnFalseForInvalidInput(String invalid) {
            boolean result = geometryService.isGeometry(invalid);

            assertThat(result).isFalse();
        }
    }

    @Nested
    class GetBounds {

        @Test
        void shouldReturnEnvelopeForValidWktPolygon() {
            String wkt = "POLYGON ((0 0, 4 0, 4 3, 0 3, 0 0))";

            Optional<Envelope> result = geometryService.getBounds(wkt);

            assertThat(result).isPresent();
            assertThat(result.get().getMinX()).isEqualTo(0.0);
            assertThat(result.get().getMinY()).isEqualTo(0.0);
            assertThat(result.get().getMaxX()).isEqualTo(4.0);
            assertThat(result.get().getMaxY()).isEqualTo(3.0);
        }

        @Test
        void shouldReturnEnvelopeForValidWktPoint() {
            String wkt = "POINT (4.3517 50.8503)";

            Optional<Envelope> result = geometryService.getBounds(wkt);

            assertThat(result).isPresent();
            assertThat(result.get().getMinX()).isEqualTo(4.3517);
            assertThat(result.get().getMinY()).isEqualTo(50.8503);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not a geometry",
            ""
        })
        void shouldReturnEmptyForInvalidInput(String invalid) {
            Optional<Envelope> result = geometryService.getBounds(invalid);

            assertThat(result).isEmpty();
        }
    }
}
