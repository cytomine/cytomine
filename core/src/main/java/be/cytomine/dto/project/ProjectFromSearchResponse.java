package be.cytomine.dto.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectFromSearchResponse {

    ProjectReference project;
    TaskReference task;

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectReference {
        Long id;
        String name;
    }

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskReference {
        Long id;
        int progress;
        Long project;
        Long user;
        boolean printInActivity;
    }
}