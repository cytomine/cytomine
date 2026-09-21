package be.cytomine.dto.project;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectFromSearchRequest {
    private String name;
    private String ontologyMode;
    private Long ontologyId;
    private String query;
    private List<String> filters;
}