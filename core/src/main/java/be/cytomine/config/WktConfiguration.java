package be.cytomine.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WktConfiguration {
    @Bean
    public WKTReader wktReader() {
        return new WKTReader(new GeometryFactory(new PrecisionModel(), 0));
    }
}
