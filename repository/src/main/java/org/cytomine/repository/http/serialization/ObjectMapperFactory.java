package org.cytomine.repository.http.serialization;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;

@Configuration
public class ObjectMapperFactory {

    @Bean
    JsonMapperBuilderCustomizer snakeCaseCustomizer() {
        return builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }


}
