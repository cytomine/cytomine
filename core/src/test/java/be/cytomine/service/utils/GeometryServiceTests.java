package be.cytomine.service.utils;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import be.cytomine.exceptions.WrongArgumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON;

public class GeometryServiceTests {

    private final GeometryService geometryService = new GeometryService(new GeoJsonWriter());

    @Nested
    class ParseTests {

        @Test
        void shouldReturnGeometryForValidWkt() {
            String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

            Optional<Geometry> result = geometryService.parse(wkt);

            assertThat(result).isPresent();
            assertThat(result.get().getGeometryType()).isEqualTo(TYPENAME_POLYGON);
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
            assertThat(result.get().getGeometryType()).isEqualTo(TYPENAME_POLYGON);
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
    class GetBoundsTests {

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

    @Nested
    class AddOffsetTests {

        @Test
        void shouldShiftCoordinatesForPositiveOffset() {
            String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

            Optional<Geometry> result = geometryService.addOffset(wkt, 10, 20);

            assertThat(result).isPresent();
            assertThat(result.get().getCoordinates()[0].x).isEqualTo(10.0);
            assertThat(result.get().getCoordinates()[0].y).isEqualTo(20.0);
        }

        @Test
        void shouldShiftCoordinatesForNegativeOffset() {
            String wkt = "POLYGON ((10 20, 11 20, 11 21, 10 21, 10 20))";

            Optional<Geometry> result = geometryService.addOffset(wkt, -5, -10);

            assertThat(result).isPresent();
            assertThat(result.get().getCoordinates()[0].x).isEqualTo(5.0);
            assertThat(result.get().getCoordinates()[0].y).isEqualTo(10.0);
        }

        @Test
        void shouldNotChangeCoordinatesForZeroOffset() {
            String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

            Optional<Geometry> result = geometryService.addOffset(wkt, 0, 0);

            assertThat(result).isPresent();
            assertThat(result.get().getCoordinates()[0].x).isEqualTo(0.0);
            assertThat(result.get().getCoordinates()[0].y).isEqualTo(0.0);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not a geometry",
            ""
        })
        void shouldReturnEmptyForInvalidWkt(String invalid) {
            Optional<Geometry> result = geometryService.addOffset(invalid, 10, 20);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class WktToGeoJsonTests {

        @Test
        void shouldConvertWktPolygonToGeojson() {
            String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

            String result = geometryService.wktToGeoJson(wkt);

            assertThat(result).contains("\"type\":\"Polygon\"");
            assertThat(result).contains("\"coordinates\"");
        }

        @Test
        void shouldConvertWktPointToGeojson() {
            String wkt = "POINT (4.3517 50.8503)";

            String result = geometryService.wktToGeoJson(wkt);

            assertThat(result).contains("\"type\":\"Point\"");
            assertThat(result).contains("4.3517");
            assertThat(result).contains("50.8503");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not a wkt",
            ""
        })
        void shouldThrowWrongArgumentExceptionForInvalidWkt(String invalid) {
            assertThrows(WrongArgumentException.class, () -> geometryService.wktToGeoJson(invalid));
        }
    }

    @Nested
    class GeoJsonToWktTests {

        @Test
        void shouldConvertGeojsonPolygonToWkt() {
            String geoJson = """
                {
                  "type": "Polygon",
                  "coordinates": [[[0,0],[1,0],[1,1],[0,1],[0,0]]]
                }
                """;

            String result = geometryService.geoJsonToWkt(geoJson);

            assertThat(result).startsWith("POLYGON");
            assertThat(result).contains("0 0");
        }

        @Test
        void shouldConvertGeojsonPointToWkt() {
            String geoJson = """
                {
                  "type": "Point",
                  "coordinates": [4.3517, 50.8503]
                }
                """;

            String result = geometryService.geoJsonToWkt(geoJson);

            assertThat(result).startsWith("POINT");
            assertThat(result).contains("4.3517");
            assertThat(result).contains("50.8503");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not a geojson",
            ""
        })
        void shouldThrowIllegalArgumentExceptionForInvalidGeojson(String invalid) {
            assertThrows(IllegalArgumentException.class, () -> geometryService.geoJsonToWkt(invalid));
        }
    }
}
