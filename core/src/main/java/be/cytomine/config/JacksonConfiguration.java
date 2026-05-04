package be.cytomine.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import be.cytomine.dto.appengine.task.input.TaskRunInput;
import be.cytomine.dto.appengine.task.input.TaskRunInputMixin;
import be.cytomine.dto.appengine.task.type.TaskParameterType;
import be.cytomine.dto.appengine.task.type.TaskParameterTypeMixin;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
            .mixIn(TaskParameterType.class, TaskParameterTypeMixin.class)
            .mixIn(TaskRunInput.class, TaskRunInputMixin.class)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
