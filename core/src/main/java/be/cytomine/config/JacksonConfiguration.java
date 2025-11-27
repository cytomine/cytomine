package be.cytomine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.cytomine.dto.appengine.task.type.TaskParameterType;
import be.cytomine.dto.appengine.task.type.TaskParameterTypeMixin;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(TaskParameterType.class, TaskParameterTypeMixin.class);
        return mapper;
    }
}
