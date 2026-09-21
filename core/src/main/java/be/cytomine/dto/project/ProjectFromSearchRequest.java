package be.cytomine.dto.project;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProjectFromSearchRequest(
    String name,
    String ontologyMode,
    Long ontologyId,
    String query,
    List<String> filters
) {
}