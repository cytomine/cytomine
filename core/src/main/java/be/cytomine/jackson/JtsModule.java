package be.cytomine.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

@Component
public class JtsModule extends SimpleModule {
    public JtsModule() {
        addSerializer(Geometry.class, new GeometrySerializer());
        addDeserializer(Geometry.class, new GeometryDeserializer());
    }
}
