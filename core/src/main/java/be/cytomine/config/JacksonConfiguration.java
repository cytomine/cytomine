package be.cytomine.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import be.cytomine.dto.appengine.task.type.TaskParameterType;
import be.cytomine.dto.appengine.task.type.TaskParameterTypeMixin;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.mixIn(TaskParameterType.class, TaskParameterTypeMixin.class);
    }
}
