package org.cytomine.repository.http.serialization;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static tools.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_WITH_ZONE_ID;

@Configuration
public class ObjectMapperFactory {

    @Bean
    JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"))
            .defaultTimeZone(TimeZone.getDefault())
            .propertyNamingStrategy(LOWER_CAMEL_CASE)
            .disable(WRITE_DATES_WITH_ZONE_ID)
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .disable(WRITE_DATES_WITH_CONTEXT_TIME_ZONE);
    }
}
