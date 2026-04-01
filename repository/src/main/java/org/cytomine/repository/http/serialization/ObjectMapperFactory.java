package org.cytomine.repository.http.serialization;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static tools.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_WITH_ZONE_ID;

@Configuration
public class ObjectMapperFactory {

    @Bean
    JsonMapperBuilderCustomizer snakeCaseCustomizer() {
        return builder -> builder
                              .propertyNamingStrategy(LOWER_CAMEL_CASE)
                              .disable(WRITE_DATES_WITH_ZONE_ID);
    }
}
