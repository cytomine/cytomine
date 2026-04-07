package org.cytomine.repository.config;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class Jackson3FormatMapper implements FormatMapper {

    private final ObjectMapper objectMapper;

    public Jackson3FormatMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return objectMapper.readValue(charSequence.toString(),
                objectMapper.constructType(javaType.getJavaType()));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not deserialize JSON: " + charSequence, e);
        }
    }

    @Override
    public <T> String toString(T value, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not serialize to JSON: " + value, e);
        }
    }
}
