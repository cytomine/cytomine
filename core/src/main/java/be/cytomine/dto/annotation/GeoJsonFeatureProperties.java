package be.cytomine.dto.annotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeoJsonFeatureProperties(
    String unit,
    @JsonProperty("path_class_name") String pathClassName
) {}
