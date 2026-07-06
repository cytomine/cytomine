package be.cytomine.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import be.cytomine.dto.appengine.task.output.TaskRunOutput;
import be.cytomine.dto.appengine.task.output.TaskRunOutputMixin;
import be.cytomine.dto.appengine.task.type.TaskParameterType;
import be.cytomine.dto.appengine.task.type.TaskParameterTypeMixin;

@Configuration
public class JacksonConfiguration {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
            .mixIn(TaskParameterType.class, TaskParameterTypeMixin.class)
            .mixIn(TaskRunOutput.class, TaskRunOutputMixin.class)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
    }
}
