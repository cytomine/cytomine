package be.cytomine.service.utils;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.stereotype.Service;

import be.cytomine.domain.annotation.GeometryType;
import be.cytomine.exceptions.WrongArgumentException;

@RequiredArgsConstructor
@Service
public class GeometryService {

    private final GeoJsonWriter geoJsonWriter;

    private static Geometry parseWKT(String wkt) {
        try {
            return new WKTReader().read(wkt);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static Geometry parseGeoJSON(String geojson) {
        try {
            return new GeoJsonReader().read(geojson);
        } catch (ParseException ignored) {
            return null;
        }
    }

    public static Envelope getBounds(String wkt) {
        Geometry geometry = parseWKT(wkt);
        return geometry.getEnvelopeInternal();
    }

    public static Geometry addOffset(String geom, Integer xOffset, Integer yOffset) {
        Geometry geometry = parseWKT(geom);

        geometry.apply((Coordinate c) -> {
            c.x += xOffset;
            c.y += yOffset;
        });
        geometry.geometryChanged();
        return geometry;
    }

    public Boolean isGeometry(String input) {
        Geometry geometry = parseWKT(input) != null ? parseWKT(input) : parseGeoJSON(input);
        return geometry != null && GeometryType.isSupported(geometry);
    }

    public String wktToGeoJson(String wkt) {
        try {
            WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(wkt);

            return geoJsonWriter.write(geometry);
        } catch (ParseException e) {
            throw new WrongArgumentException("WKT cannot be convert to GeoJSON: " + wkt);
        }
    }

    public String geoJsonToWkt(String geoJSON) {
        try {
            GeoJsonReader reader = new GeoJsonReader();
            Geometry geometry = reader.read(geoJSON);

            return new WKTWriter().write(geometry);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid GeoJSON string: " + geoJSON);
        }
    }
}
