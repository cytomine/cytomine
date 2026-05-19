package be.cytomine.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class GeometrySerializer extends JsonSerializer<Geometry> {
    private final GeoJsonWriter writer = new GeoJsonWriter();

    @Override
    public void serialize(Geometry geometry, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeRawValue(writer.write(geometry));
    }
}