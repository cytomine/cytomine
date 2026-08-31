package be.cytomine.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {
    private final GeoJsonReader reader = new GeoJsonReader();

    @Override
    public Geometry deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        try {
            return reader.read(parser.getText());
        } catch (Exception e) {
            throw new IOException("Failed to deserialize GeoJSON geometry", e);
        }
    }
}
