package be.cytomine.config;

import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoJsonConfiguration {
    @Bean
    public GeoJsonWriter geoJsonWriter() {
        GeoJsonWriter writer = new GeoJsonWriter();
        writer.setEncodeCRS(false);
        return writer;
    }
}
