package be.cytomine.dto.appengine.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskRunOutputResponse(
    String id,
    String name,
    @JsonProperty(value = "display_name") String displayName,
    String description,
    boolean optional,
    JsonNode type,
    @JsonProperty(value = "derived_from") String derivedFrom
) {}
