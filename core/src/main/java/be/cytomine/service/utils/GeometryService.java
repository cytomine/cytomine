package be.cytomine.service.utils;

import java.util.Optional;

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

    private Optional<Geometry> parseWKT(String wkt) {
        try {
            return Optional.of(new WKTReader().read(wkt));
        } catch (ParseException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Geometry> parseGeoJSON(String geojson) {
        try {
            return Optional.of(new GeoJsonReader().read(geojson));
        } catch (ParseException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Geometry> parse(String input) {
        return parseWKT(input).or(() -> parseGeoJSON(input));
    }

    public Optional<Envelope> getBounds(String input) {
        return parse(input).map(Geometry::getEnvelopeInternal);
    }

    public Optional<Geometry> addOffset(String input, Integer xOffset, Integer yOffset) {
        return parseWKT(input).map(g -> {
            g.apply((Coordinate c) -> {
                c.x += xOffset;
                c.y += yOffset;
            });
            g.geometryChanged();
            return g;
        });
    }

    public Optional<Geometry> addOffset(Geometry geometry, Integer xOffset, Integer yOffset) {
        geometry.apply((Coordinate c) -> {
            c.x += xOffset;
            c.y += yOffset;
        });
        geometry.geometryChanged();
        return Optional.of(geometry);
    }

    public boolean isGeometry(String input) {
        return parse(input)
            .map(GeometryType::isSupported)
            .orElse(false);
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
