package be.cytomine.appengine.dto.inputs.task;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import be.cytomine.appengine.models.task.ValueType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRunParameterValue {
    protected UUID taskRunId;

    protected String parameterName;

    protected ValueType type;
}
