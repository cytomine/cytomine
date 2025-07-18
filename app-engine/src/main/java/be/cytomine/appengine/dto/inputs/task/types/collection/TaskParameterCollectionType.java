package be.cytomine.appengine.dto.inputs.task.types.collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import be.cytomine.appengine.dto.inputs.task.TaskParameterType;
import be.cytomine.appengine.models.task.Type;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TaskParameterCollectionType extends TaskParameterType {
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer minSize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxSize;

    private Type subType;
}
