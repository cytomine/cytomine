package be.cytomine.appengine.dto.inputs.task.types.collection;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import be.cytomine.appengine.dto.inputs.task.TaskRunParameterValue;
import be.cytomine.appengine.models.task.ValueType;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionValue extends TaskRunParameterValue {
    private List<TaskRunParameterValue> value;
    private ValueType subType;
}
