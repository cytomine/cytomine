package be.cytomine.appengine.dto.inputs.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenericParameterProvision extends ParameterProvision {
    private String parameterName;

    private Object value; // this should be Object now to accommodate other primitive types

    @JsonIgnore
    private String runId;
}
